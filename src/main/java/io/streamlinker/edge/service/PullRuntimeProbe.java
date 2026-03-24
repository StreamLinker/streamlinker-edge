package io.streamlinker.edge.service;

import io.streamlinker.edge.infra.db.entity.StreamEntity;
import org.springframework.stereotype.Component;

@Component
public class PullRuntimeProbe {

    private final PullExecutionGateway pullExecutionGateway;

    public PullRuntimeProbe(PullExecutionGateway pullExecutionGateway) {
        this.pullExecutionGateway = pullExecutionGateway;
    }

    public boolean isOnline(StreamEntity stream) {
        return pullExecutionGateway.isOnline(stream);
    }
}