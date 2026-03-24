package io.streamlinker.edge.service;

import io.streamlinker.edge.domain.AccessMode;
import io.streamlinker.edge.domain.StreamDefinition;
import io.streamlinker.edge.domain.StreamRuntime;
import io.streamlinker.edge.domain.StreamState;
import io.streamlinker.edge.infra.db.entity.StreamEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@ConditionalOnProperty(prefix = "streamlinker.edge.media", name = "simulation-enabled", havingValue = "true", matchIfMissing = true)
public class SimulatedPullExecutionGateway implements PullExecutionGateway {

    private final LocalPullExecutionRegistry executionRegistry;

    public SimulatedPullExecutionGateway(LocalPullExecutionRegistry executionRegistry) {
        this.executionRegistry = executionRegistry;
    }

    @Override
    public StreamRuntime startFfmpeg(StreamDefinition definition, StreamRuntime currentRuntime) {
        String taskKey = "ffmpeg-" + definition.getStreamId();
        executionRegistry.markRunning(definition.getStreamId(), definition.getAccessMode(), taskKey);
        return buildRuntime(definition, StreamState.RUNNING, taskKey, null, true);
    }

    @Override
    public StreamRuntime startProxy(StreamDefinition definition, StreamRuntime currentRuntime) {
        String taskKey = "proxy-" + definition.getStreamId();
        executionRegistry.markRunning(definition.getStreamId(), definition.getAccessMode(), taskKey);
        return buildRuntime(definition, StreamState.RUNNING, null, taskKey, true);
    }

    @Override
    public StreamRuntime stop(StreamDefinition definition, StreamRuntime currentRuntime) {
        executionRegistry.markStopped(definition.getStreamId());
        return buildRuntime(definition, StreamState.IDLE, null, null, false);
    }

    @Override
    public boolean isOnline(StreamEntity stream) {
        return executionRegistry.isOnline(stream.getStreamCode());
    }

    private StreamRuntime buildRuntime(StreamDefinition definition,
                                       StreamState state,
                                       String zlmTaskKey,
                                       String proxyKey,
                                       boolean online) {
        return StreamRuntime.builder()
                .streamId(definition.getStreamId())
                .accessMode(definition.getAccessMode())
                .state(state)
                .zlmTaskKey(zlmTaskKey)
                .proxyKey(proxyKey)
                .localOnline(online)
                .cloudOnline(false)
                .updatedAt(Instant.now())
                .build();
    }
}