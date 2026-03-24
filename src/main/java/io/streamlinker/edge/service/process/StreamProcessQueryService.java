package io.streamlinker.edge.service.process;

import io.streamlinker.edge.infra.db.entity.StreamProcessEntity;
import io.streamlinker.edge.infra.db.repository.StreamProcessRepository;
import org.springframework.stereotype.Service;

@Service
public class StreamProcessQueryService {

    private final StreamProcessRepository streamProcessRepository;

    public StreamProcessQueryService(StreamProcessRepository streamProcessRepository) {
        this.streamProcessRepository = streamProcessRepository;
    }

    public StreamProcessEntity findActivePullProcess(Long streamId, String processType) {
        return streamProcessRepository.findActiveByStreamIdAndType(streamId, processType);
    }

    public StreamProcessEntity findActivePushProcess(Long pushTargetId, String processType) {
        return streamProcessRepository.findActiveByPushTargetIdAndType(pushTargetId, processType);
    }
}