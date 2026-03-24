package io.streamlinker.edge.service;

import io.streamlinker.edge.infra.db.entity.StreamEntity;
import io.streamlinker.edge.infra.db.entity.StreamPushRuntimeEntity;
import io.streamlinker.edge.infra.db.entity.StreamPushTargetEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "streamlinker.edge.media", name = "simulation-enabled", havingValue = "true", matchIfMissing = true)
public class SimulatedPushExecutionGateway implements PushExecutionGateway {

    private final LocalPushExecutionRegistry executionRegistry;

    public SimulatedPushExecutionGateway(LocalPushExecutionRegistry executionRegistry) {
        this.executionRegistry = executionRegistry;
    }

    @Override
    public String startPush(StreamEntity stream, StreamPushTargetEntity target) {
        String pusherKey = "push-" + target.getTargetCode();
        executionRegistry.markRunning(target.getTargetCode(), pusherKey);
        return pusherKey;
    }

    @Override
    public void stopPush(StreamPushTargetEntity target, StreamPushRuntimeEntity runtime) {
        executionRegistry.markStopped(target.getTargetCode());
    }

    @Override
    public boolean isOnline(StreamPushTargetEntity target) {
        return executionRegistry.isOnline(target.getTargetCode());
    }
}