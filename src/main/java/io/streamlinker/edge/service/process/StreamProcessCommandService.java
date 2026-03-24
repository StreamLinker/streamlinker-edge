package io.streamlinker.edge.service.process;

import io.streamlinker.edge.domain.ProcessStatus;
import io.streamlinker.edge.domain.ProcessType;
import io.streamlinker.edge.infra.db.entity.StreamProcessEntity;
import io.streamlinker.edge.infra.db.repository.StreamProcessRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class StreamProcessCommandService {

    private static final int DEFAULT_MAX_RETRY_COUNT = 10;

    private final StreamProcessRepository streamProcessRepository;

    public StreamProcessCommandService(StreamProcessRepository streamProcessRepository) {
        this.streamProcessRepository = streamProcessRepository;
    }

    public StreamProcessEntity createPullProcess(Long processId, Long streamId, ProcessType processType) {
        StreamProcessEntity existing = streamProcessRepository.findActiveByStreamIdAndType(streamId, processType.name());
        if (existing != null) {
            return existing;
        }
        StreamProcessEntity entity = new StreamProcessEntity();
        entity.setId(processId);
        entity.setProcessType(processType.name());
        entity.setStreamId(streamId);
        entity.setStep(0);
        entity.setStatus(ProcessStatus.INIT.name());
        entity.setRetryCount(0);
        entity.setMaxRetryCount(DEFAULT_MAX_RETRY_COUNT);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        return streamProcessRepository.create(entity);
    }

    public StreamProcessEntity createPushProcess(Long processId, Long pushTargetId, ProcessType processType) {
        StreamProcessEntity existing = streamProcessRepository.findActiveByPushTargetIdAndType(pushTargetId, processType.name());
        if (existing != null) {
            return existing;
        }
        StreamProcessEntity entity = new StreamProcessEntity();
        entity.setId(processId);
        entity.setProcessType(processType.name());
        entity.setPushTargetId(pushTargetId);
        entity.setStep(0);
        entity.setStatus(ProcessStatus.INIT.name());
        entity.setRetryCount(0);
        entity.setMaxRetryCount(DEFAULT_MAX_RETRY_COUNT);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        return streamProcessRepository.create(entity);
    }

    public StreamProcessEntity findById(Long processId) {
        return streamProcessRepository.findById(processId);
    }

    public StreamProcessEntity markRunning(Long processId) {
        StreamProcessEntity entity = requireProcess(processId);
        entity.setStatus(ProcessStatus.RUNNING.name());
        if (entity.getStartTime() == null) {
            entity.setStartTime(LocalDateTime.now());
        }
        entity.setUpdateTime(LocalDateTime.now());
        return streamProcessRepository.save(entity);
    }

    public StreamProcessEntity advanceStep(Long processId, int nextStep, String contextSnapshot) {
        StreamProcessEntity entity = requireProcess(processId);
        entity.setStep(nextStep);
        entity.setContextSnapshot(contextSnapshot);
        entity.setUpdateTime(LocalDateTime.now());
        return streamProcessRepository.save(entity);
    }

    public StreamProcessEntity markSuccess(Long processId, int finalStep, String contextSnapshot) {
        StreamProcessEntity entity = requireProcess(processId);
        entity.setStep(finalStep);
        entity.setStatus(ProcessStatus.SUCCESS.name());
        entity.setContextSnapshot(contextSnapshot);
        entity.setErrorMessage(null);
        entity.setFinishTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        return streamProcessRepository.save(entity);
    }

    public StreamProcessEntity markFailed(Long processId, int failedStep, String errorMessage) {
        StreamProcessEntity entity = requireProcess(processId);
        entity.setStep(failedStep);
        entity.setStatus(ProcessStatus.FAILED.name());
        entity.setErrorMessage(errorMessage);
        entity.setFinishTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        return streamProcessRepository.save(entity);
    }

    public StreamProcessEntity incrementRetryCount(Long processId, String errorMessage) {
        StreamProcessEntity entity = requireProcess(processId);
        entity.setRetryCount(entity.getRetryCount() == null ? 1 : entity.getRetryCount() + 1);
        entity.setErrorMessage(errorMessage);
        entity.setUpdateTime(LocalDateTime.now());
        return streamProcessRepository.save(entity);
    }

    private StreamProcessEntity requireProcess(Long processId) {
        StreamProcessEntity entity = streamProcessRepository.findById(processId);
        if (entity == null) {
            throw new IllegalArgumentException("Unknown processId: " + processId);
        }
        return entity;
    }
}