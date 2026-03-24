package io.streamlinker.edge.service;

import io.streamlinker.edge.domain.AccessMode;
import io.streamlinker.edge.domain.ExpectedState;
import io.streamlinker.edge.domain.ProcessStatus;
import io.streamlinker.edge.domain.ProcessType;
import io.streamlinker.edge.domain.StreamDefinition;
import io.streamlinker.edge.domain.StreamRuntime;
import io.streamlinker.edge.domain.StreamState;
import io.streamlinker.edge.infra.db.entity.StreamEntity;
import io.streamlinker.edge.infra.db.entity.StreamProcessEntity;
import io.streamlinker.edge.infra.db.entity.StreamRuntimeEntity;
import io.streamlinker.edge.infra.db.repository.StreamRepository;
import io.streamlinker.edge.infra.db.repository.StreamRuntimeRepository;
import io.streamlinker.edge.service.process.StreamProcessCommandService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class StreamOrchestrator {

    private final StreamRepository streamRepository;
    private final StreamRuntimeRepository streamRuntimeRepository;
    private final StreamProcessCommandService streamProcessCommandService;
    private final List<AccessModeHandler> handlers;
    private final AtomicLong processIdGenerator = new AtomicLong(1);

    public StreamOrchestrator(StreamRepository streamRepository,
                              StreamRuntimeRepository streamRuntimeRepository,
                              StreamProcessCommandService streamProcessCommandService,
                              List<AccessModeHandler> handlers) {
        this.streamRepository = streamRepository;
        this.streamRuntimeRepository = streamRuntimeRepository;
        this.streamProcessCommandService = streamProcessCommandService;
        this.handlers = handlers;
    }

    public StreamRuntime start(String streamId) {
        StreamEntity entity = requireEntity(streamId);
        updateExpectedState(entity, ExpectedState.RUNNING);
        StreamDefinition definition = toDefinition(entity);
        StreamRuntime current = getCurrentRuntime(entity);
        if (current != null && current.getState() == StreamState.RUNNING) {
            return current;
        }
        StreamProcessEntity process = streamProcessCommandService.createPullProcess(
                processIdGenerator.getAndIncrement(),
                entity.getId(),
                ProcessType.PULL_UP);
        return executePullUp(process, entity, definition, current);
    }

    public StreamRuntime stop(String streamId) {
        StreamEntity entity = requireEntity(streamId);
        updateExpectedState(entity, ExpectedState.STOPPED);
        StreamDefinition definition = toDefinition(entity);
        StreamRuntime current = getCurrentRuntime(entity);
        if (current != null && current.getState() == StreamState.IDLE) {
            return current;
        }
        StreamProcessEntity process = streamProcessCommandService.createPullProcess(
                processIdGenerator.getAndIncrement(),
                entity.getId(),
                ProcessType.PULL_DOWN);
        return executePullDown(process, entity, definition, current);
    }

    public StreamRuntime restart(String streamId) {
        stop(streamId);
        return start(streamId);
    }

    public List<StreamRuntime> list() {
        Map<Long, StreamEntity> streamsById = streamRepository.findAll().stream()
                .collect(Collectors.toMap(StreamEntity::getId, Function.identity()));
        return streamRuntimeRepository.findAll().stream()
                .map(runtime -> toRuntime(streamsById.get(runtime.getStreamId()), runtime))
                .filter(runtime -> runtime.getStreamId() != null)
                .sorted(Comparator.comparing(StreamRuntime::getStreamId))
                .toList();
    }

    private StreamRuntime getCurrentRuntime(StreamEntity entity) {
        StreamRuntimeEntity runtimeEntity = streamRuntimeRepository.findByStreamId(entity.getId());
        return runtimeEntity == null ? null : toRuntime(entity, runtimeEntity);
    }

    private StreamRuntime executePullUp(StreamProcessEntity process,
                                        StreamEntity entity,
                                        StreamDefinition definition,
                                        StreamRuntime current) {
        try {
            streamProcessCommandService.markRunning(process.getId());
            streamProcessCommandService.advanceStep(process.getId(), 1, "{\"phase\":\"handler-start\"}");
            AccessModeHandler handler = resolveHandler(definition);
            StreamRuntime runtime = handler.start(definition, current);
            StreamRuntime persisted = persistRuntime(entity, definition, runtime);
            streamProcessCommandService.advanceStep(process.getId(), 2, "{\"phase\":\"local-online-check\"}");
            if (!Boolean.TRUE.equals(persisted.getLocalOnline()) || persisted.getState() != StreamState.RUNNING) {
                return failProcess(process.getId(), 2, definition.getStreamId(), "Local stream is not online after pull-up execution");
            }
            streamProcessCommandService.markSuccess(process.getId(), 3, "{\"phase\":\"completed\"}");
            return persisted;
        } catch (RuntimeException ex) {
            handleUnexpectedFailure(process.getId(), ex.getMessage());
            throw ex;
        }
    }

    private StreamRuntime executePullDown(StreamProcessEntity process,
                                          StreamEntity entity,
                                          StreamDefinition definition,
                                          StreamRuntime current) {
        try {
            streamProcessCommandService.markRunning(process.getId());
            streamProcessCommandService.advanceStep(process.getId(), 1, "{\"phase\":\"handler-stop\"}");
            AccessModeHandler handler = resolveHandler(definition);
            StreamRuntime runtime = handler.stop(definition, current);
            StreamRuntime persisted = persistRuntime(entity, definition, runtime);
            streamProcessCommandService.advanceStep(process.getId(), 2, "{\"phase\":\"local-offline-check\"}");
            if (Boolean.TRUE.equals(persisted.getLocalOnline()) || persisted.getState() != StreamState.IDLE) {
                return failProcess(process.getId(), 2, definition.getStreamId(), "Local stream is still online after pull-down execution");
            }
            streamProcessCommandService.markSuccess(process.getId(), 3, "{\"phase\":\"completed\"}");
            return persisted;
        } catch (RuntimeException ex) {
            handleUnexpectedFailure(process.getId(), ex.getMessage());
            throw ex;
        }
    }

    private void handleUnexpectedFailure(Long processId, String message) {
        StreamProcessEntity latest = streamProcessCommandService.findById(processId);
        if (latest == null || !ProcessStatus.FAILED.name().equals(latest.getStatus())) {
            streamProcessCommandService.incrementRetryCount(processId, message);
            streamProcessCommandService.markFailed(processId, 1, message);
        }
    }

    private StreamRuntime failProcess(Long processId, int failedStep, String streamId, String message) {
        streamProcessCommandService.incrementRetryCount(processId, message);
        streamProcessCommandService.markFailed(processId, failedStep, message);
        throw new IllegalStateException("Process failed for streamId=" + streamId + ": " + message);
    }

    private StreamRuntime persistRuntime(StreamEntity entity, StreamDefinition definition, StreamRuntime runtime) {
        StreamRuntimeEntity runtimeEntity = new StreamRuntimeEntity();
        runtimeEntity.setStreamId(entity.getId());
        runtimeEntity.setPullStatus(runtime.getState().name());
        runtimeEntity.setMediaKey(buildMediaKey(definition));
        runtimeEntity.setZlmTaskKey(runtime.getZlmTaskKey() != null ? runtime.getZlmTaskKey() : runtime.getProxyKey());
        runtimeEntity.setLocalOnline(Boolean.TRUE.equals(runtime.getLocalOnline()) ? 1 : 0);
        runtimeEntity.setLastOnlineAt(Boolean.TRUE.equals(runtime.getLocalOnline()) ? LocalDateTime.ofInstant(runtime.getUpdatedAt(), ZoneOffset.UTC) : null);
        runtimeEntity.setLastError(runtime.getLastError());
        runtimeEntity.setReconcileVersion(0L);
        runtimeEntity.setUpdateTime(LocalDateTime.ofInstant(runtime.getUpdatedAt(), ZoneOffset.UTC));
        streamRuntimeRepository.saveOrUpdate(runtimeEntity);
        return toRuntime(entity, streamRuntimeRepository.findByStreamId(entity.getId()));
    }

    private void updateExpectedState(StreamEntity entity, ExpectedState expectedState) {
        entity.setExpectedState(expectedState.name());
        streamRepository.save(entity);
    }

    private AccessModeHandler resolveHandler(StreamDefinition definition) {
        return handlers.stream()
                .filter(handler -> handler.supports(definition))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No handler for accessMode=" + definition.getAccessMode()));
    }

    private StreamEntity requireEntity(String streamId) {
        StreamEntity entity = streamRepository.findByStreamId(streamId);
        if (entity == null) {
            throw new IllegalArgumentException("Unknown streamId: " + streamId);
        }
        return entity;
    }

    private StreamDefinition toDefinition(StreamEntity entity) {
        return StreamDefinition.builder()
                .streamId(entity.getStreamCode())
                .name(entity.getName())
                .enabled(entity.getEnabled() != null && entity.getEnabled() == 1)
                .accessMode(AccessMode.valueOf(entity.getAccessMode()))
                .sourceUrl(entity.getSourceUrl())
                .localApp(entity.getLocalApp())
                .localStream(entity.getLocalStream())
                .remark(entity.getRemark())
                .build();
    }

    private StreamRuntime toRuntime(StreamEntity streamEntity, StreamRuntimeEntity runtimeEntity) {
        if (streamEntity == null || runtimeEntity == null) {
            return StreamRuntime.builder().build();
        }
        AccessMode accessMode = AccessMode.valueOf(streamEntity.getAccessMode());
        String taskKey = runtimeEntity.getZlmTaskKey();
        return StreamRuntime.builder()
                .streamId(streamEntity.getStreamCode())
                .state(StreamState.valueOf(runtimeEntity.getPullStatus()))
                .accessMode(accessMode)
                .zlmTaskKey(accessMode == AccessMode.FFMPEG ? taskKey : null)
                .proxyKey(accessMode == AccessMode.PROXY ? taskKey : null)
                .localOnline(runtimeEntity.getLocalOnline() != null && runtimeEntity.getLocalOnline() == 1)
                .cloudOnline(false)
                .lastError(runtimeEntity.getLastError())
                .updatedAt(runtimeEntity.getUpdateTime() == null ? Instant.now() : runtimeEntity.getUpdateTime().toInstant(ZoneOffset.UTC))
                .build();
    }

    private String buildMediaKey(StreamDefinition definition) {
        return definition.getLocalApp() + "/" + definition.getLocalStream();
    }
}