package io.streamlinker.edge.service;

import io.streamlinker.edge.domain.ExpectedState;
import io.streamlinker.edge.domain.ProcessStatus;
import io.streamlinker.edge.domain.ProcessType;
import io.streamlinker.edge.domain.PushStatus;
import io.streamlinker.edge.infra.db.entity.StreamEntity;
import io.streamlinker.edge.infra.db.entity.StreamProcessEntity;
import io.streamlinker.edge.infra.db.entity.StreamPushRuntimeEntity;
import io.streamlinker.edge.infra.db.entity.StreamPushTargetEntity;
import io.streamlinker.edge.infra.db.entity.StreamRuntimeEntity;
import io.streamlinker.edge.infra.db.repository.StreamPushRuntimeRepository;
import io.streamlinker.edge.infra.db.repository.StreamPushTargetRepository;
import io.streamlinker.edge.infra.db.repository.StreamRepository;
import io.streamlinker.edge.infra.db.repository.StreamRuntimeRepository;
import io.streamlinker.edge.service.process.StreamProcessCommandService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PushTargetOrchestrator {

    private final StreamPushTargetRepository streamPushTargetRepository;
    private final StreamPushRuntimeRepository streamPushRuntimeRepository;
    private final StreamRepository streamRepository;
    private final StreamRuntimeRepository streamRuntimeRepository;
    private final StreamProcessCommandService streamProcessCommandService;
    private final StreamOrchestrator streamOrchestrator;
    private final PushExecutionGateway pushExecutionGateway;
    private final AtomicLong processIdGenerator = new AtomicLong(100000);

    public PushTargetOrchestrator(StreamPushTargetRepository streamPushTargetRepository,
                                  StreamPushRuntimeRepository streamPushRuntimeRepository,
                                  StreamRepository streamRepository,
                                  StreamRuntimeRepository streamRuntimeRepository,
                                  StreamProcessCommandService streamProcessCommandService,
                                  StreamOrchestrator streamOrchestrator,
                                  PushExecutionGateway pushExecutionGateway) {
        this.streamPushTargetRepository = streamPushTargetRepository;
        this.streamPushRuntimeRepository = streamPushRuntimeRepository;
        this.streamRepository = streamRepository;
        this.streamRuntimeRepository = streamRuntimeRepository;
        this.streamProcessCommandService = streamProcessCommandService;
        this.streamOrchestrator = streamOrchestrator;
        this.pushExecutionGateway = pushExecutionGateway;
    }

    public StreamPushRuntimeEntity start(String targetCode) {
        StreamPushTargetEntity target = requireTarget(targetCode);
        requireRtmp(target);
        updateExpectedState(target, ExpectedState.RUNNING);

        StreamPushRuntimeEntity current = streamPushRuntimeRepository.findByPushTargetId(target.getId());
        if (current != null && current.getOnline() != null && current.getOnline() == 1
                && PushStatus.RUNNING.name().equals(current.getPushStatus())) {
            return current;
        }

        StreamEntity stream = ensureParentPullOnline(target);
        StreamProcessEntity process = streamProcessCommandService.createPushProcess(
                processIdGenerator.getAndIncrement(),
                target.getId(),
                ProcessType.PUSH_UP);
        return executePushUp(process, stream, target);
    }

    public StreamPushRuntimeEntity stop(String targetCode) {
        StreamPushTargetEntity target = requireTarget(targetCode);
        updateExpectedState(target, ExpectedState.STOPPED);

        StreamPushRuntimeEntity current = streamPushRuntimeRepository.findByPushTargetId(target.getId());
        if (current == null || current.getOnline() == null || current.getOnline() == 0) {
            return persistRuntime(target, PushStatus.IDLE, null, false, null);
        }

        StreamProcessEntity process = streamProcessCommandService.createPushProcess(
                processIdGenerator.getAndIncrement(),
                target.getId(),
                ProcessType.PUSH_DOWN);
        return executePushDown(process, target, current);
    }

    private StreamPushRuntimeEntity executePushUp(StreamProcessEntity process, StreamEntity stream, StreamPushTargetEntity target) {
        try {
            streamProcessCommandService.markRunning(process.getId());
            streamProcessCommandService.advanceStep(process.getId(), 1, "{\"phase\":\"create-pusher\"}");
            String pusherKey = pushExecutionGateway.startPush(stream, target);
            StreamPushRuntimeEntity persisted = persistRuntime(target, PushStatus.RUNNING, pusherKey, true, null);
            streamProcessCommandService.advanceStep(process.getId(), 2, "{\"phase\":\"online-check\"}");
            if (persisted.getOnline() == null || persisted.getOnline() == 0 || !PushStatus.RUNNING.name().equals(persisted.getPushStatus())) {
                return failProcess(process.getId(), 2, target.getTargetCode(), "Push target is not online after push-up execution");
            }
            streamProcessCommandService.markSuccess(process.getId(), 3, "{\"phase\":\"completed\"}");
            return persisted;
        } catch (RuntimeException ex) {
            handleUnexpectedFailure(process.getId(), ex.getMessage());
            throw ex;
        }
    }

    private StreamPushRuntimeEntity executePushDown(StreamProcessEntity process,
                                                    StreamPushTargetEntity target,
                                                    StreamPushRuntimeEntity current) {
        try {
            streamProcessCommandService.markRunning(process.getId());
            streamProcessCommandService.advanceStep(process.getId(), 1, "{\"phase\":\"remove-pusher\"}");
            pushExecutionGateway.stopPush(target, current);
            StreamPushRuntimeEntity persisted = persistRuntime(target, PushStatus.IDLE, null, false, null);
            streamProcessCommandService.advanceStep(process.getId(), 2, "{\"phase\":\"offline-check\"}");
            if (persisted.getOnline() != null && persisted.getOnline() == 1) {
                return failProcess(process.getId(), 2, target.getTargetCode(), "Push target is still online after push-down execution");
            }
            streamProcessCommandService.markSuccess(process.getId(), 3, "{\"phase\":\"completed\"}");
            return persisted;
        } catch (RuntimeException ex) {
            handleUnexpectedFailure(process.getId(), ex.getMessage());
            throw ex;
        }
    }

    private StreamEntity ensureParentPullOnline(StreamPushTargetEntity target) {
        StreamEntity stream = requireStream(target.getStreamId());
        StreamRuntimeEntity runtime = streamRuntimeRepository.findByStreamId(target.getStreamId());
        if (runtime == null || runtime.getLocalOnline() == null || runtime.getLocalOnline() == 0) {
            streamOrchestrator.start(stream.getStreamCode());
        }
        return stream;
    }

    private StreamEntity requireStream(Long streamId) {
        return streamRepository.findAll().stream()
                .filter(entity -> streamId.equals(entity.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown stream for pushTarget, streamId=" + streamId));
    }

    private StreamPushTargetEntity requireTarget(String targetCode) {
        StreamPushTargetEntity target = streamPushTargetRepository.findByTargetCode(targetCode);
        if (target == null) {
            throw new IllegalArgumentException("Unknown targetCode: " + targetCode);
        }
        return target;
    }

    private void requireRtmp(StreamPushTargetEntity target) {
        if (!"RTMP".equalsIgnoreCase(target.getTargetProtocol())) {
            throw new IllegalArgumentException("Only RTMP push is supported in V1, targetCode=" + target.getTargetCode());
        }
    }

    private void updateExpectedState(StreamPushTargetEntity target, ExpectedState expectedState) {
        target.setExpectedState(expectedState.name());
        streamPushTargetRepository.save(target);
    }

    private StreamPushRuntimeEntity persistRuntime(StreamPushTargetEntity target,
                                                   PushStatus pushStatus,
                                                   String pusherKey,
                                                   boolean online,
                                                   String lastError) {
        StreamPushRuntimeEntity runtime = streamPushRuntimeRepository.findByPushTargetId(target.getId());
        if (runtime == null) {
            runtime = new StreamPushRuntimeEntity();
            runtime.setPushTargetId(target.getId());
        }
        runtime.setPushStatus(pushStatus.name());
        runtime.setPusherKey(pusherKey);
        runtime.setOnline(online ? 1 : 0);
        runtime.setLastOnlineAt(online ? LocalDateTime.now() : runtime.getLastOnlineAt());
        runtime.setLastError(lastError);
        runtime.setReconcileVersion(runtime.getReconcileVersion() == null ? 0L : runtime.getReconcileVersion());
        runtime.setUpdateTime(LocalDateTime.now());
        streamPushRuntimeRepository.saveOrUpdate(runtime);
        return streamPushRuntimeRepository.findByPushTargetId(target.getId());
    }

    private void handleUnexpectedFailure(Long processId, String message) {
        StreamProcessEntity latest = streamProcessCommandService.findById(processId);
        if (latest == null || !ProcessStatus.FAILED.name().equals(latest.getStatus())) {
            streamProcessCommandService.incrementRetryCount(processId, message);
            streamProcessCommandService.markFailed(processId, 1, message);
        }
    }

    private StreamPushRuntimeEntity failProcess(Long processId, int failedStep, String targetCode, String message) {
        streamProcessCommandService.incrementRetryCount(processId, message);
        streamProcessCommandService.markFailed(processId, failedStep, message);
        throw new IllegalStateException("Process failed for targetCode=" + targetCode + ": " + message);
    }
}