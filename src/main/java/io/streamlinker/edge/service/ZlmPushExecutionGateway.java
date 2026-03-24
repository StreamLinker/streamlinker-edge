package io.streamlinker.edge.service;

import io.streamlinker.edge.infra.db.entity.StreamEntity;
import io.streamlinker.edge.infra.db.entity.StreamPushRuntimeEntity;
import io.streamlinker.edge.infra.db.entity.StreamPushTargetEntity;
import io.streamlinker.zlm.api.ZlmClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "streamlinker.edge.media", name = "simulation-enabled", havingValue = "false")
public class ZlmPushExecutionGateway implements PushExecutionGateway {

    private final ZlmClient zlmClient;
    private final LocalPushExecutionRegistry executionRegistry;

    public ZlmPushExecutionGateway(ZlmClient zlmClient, LocalPushExecutionRegistry executionRegistry) {
        this.zlmClient = zlmClient;
        this.executionRegistry = executionRegistry;
    }

    @Override
    public String startPush(StreamEntity stream, StreamPushTargetEntity target) {
        String key = zlmClient.addStreamPusherProxy(
                stream.getLocalApp(),
                stream.getLocalStream(),
                "rtmp",
                target.getTargetUrl(),
                0).getKey();
        executionRegistry.markRunning(target.getTargetCode(), key);
        return key;
    }

    @Override
    public void stopPush(StreamPushTargetEntity target, StreamPushRuntimeEntity runtime) {
        if (runtime != null && runtime.getPusherKey() != null) {
            zlmClient.delStreamPusherProxy(runtime.getPusherKey());
        }
        executionRegistry.markStopped(target.getTargetCode());
    }

    @Override
    public boolean isOnline(StreamPushTargetEntity target) {
        return executionRegistry.isOnline(target.getTargetCode());
    }
}