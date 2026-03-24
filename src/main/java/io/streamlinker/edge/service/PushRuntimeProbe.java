package io.streamlinker.edge.service;

import io.streamlinker.edge.infra.db.entity.StreamPushTargetEntity;
import org.springframework.stereotype.Component;

@Component
public class PushRuntimeProbe {

    private final PushExecutionGateway pushExecutionGateway;

    public PushRuntimeProbe(PushExecutionGateway pushExecutionGateway) {
        this.pushExecutionGateway = pushExecutionGateway;
    }

    public boolean isOnline(StreamPushTargetEntity target) {
        return pushExecutionGateway.isOnline(target);
    }
}