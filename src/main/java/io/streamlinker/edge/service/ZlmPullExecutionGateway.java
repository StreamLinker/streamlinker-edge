package io.streamlinker.edge.service;

import io.streamlinker.edge.domain.AccessMode;
import io.streamlinker.edge.domain.StreamDefinition;
import io.streamlinker.edge.domain.StreamRuntime;
import io.streamlinker.edge.domain.StreamState;
import io.streamlinker.edge.infra.db.entity.StreamEntity;
import io.streamlinker.zlm.api.ZlmClient;
import io.streamlinker.zlm.api.ZlmClientException;
import io.streamlinker.zlm.model.response.StreamKeyResponse;
import io.streamlinker.zlm.spring.ZlmProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;

@Component
@ConditionalOnProperty(prefix = "streamlinker.edge.media", name = "simulation-enabled", havingValue = "false")
public class ZlmPullExecutionGateway implements PullExecutionGateway {

    private final ZlmClient zlmClient;
    private final ZlmProperties zlmProperties;

    public ZlmPullExecutionGateway(ZlmClient zlmClient, ZlmProperties zlmProperties) {
        this.zlmClient = zlmClient;
        this.zlmProperties = zlmProperties;
    }

    @Override
    public StreamRuntime startFfmpeg(StreamDefinition definition, StreamRuntime currentRuntime) {
        StreamKeyResponse response = zlmClient.addFfmpegSource(
                definition.getSourceUrl(),
                buildLocalRtmpUrl(definition),
                15000,
                false,
                false,
                null);
        return buildRuntime(definition, StreamState.RUNNING, response.getKey(), null, true, null);
    }

    @Override
    public StreamRuntime startProxy(StreamDefinition definition, StreamRuntime currentRuntime) {
        StreamKeyResponse response = zlmClient.addStreamProxy(
                definition.getLocalApp(),
                definition.getLocalStream(),
                definition.getSourceUrl(),
                0,
                false,
                true);
        return buildRuntime(definition, StreamState.RUNNING, null, response.getKey(), true, null);
    }

    @Override
    public StreamRuntime stop(StreamDefinition definition, StreamRuntime currentRuntime) {
        if (definition.getAccessMode() == AccessMode.FFMPEG && currentRuntime != null && currentRuntime.getZlmTaskKey() != null) {
            zlmClient.delFfmpegSource(currentRuntime.getZlmTaskKey());
        } else if (definition.getAccessMode() == AccessMode.PROXY && currentRuntime != null && currentRuntime.getProxyKey() != null) {
            zlmClient.delStreamProxy(currentRuntime.getProxyKey());
        } else {
            zlmClient.closeStream("rtmp", zlmProperties.getVhost(), definition.getLocalApp(), definition.getLocalStream(), true);
        }
        return buildRuntime(definition, StreamState.IDLE, null, null, false, null);
    }

    @Override
    public boolean isOnline(StreamEntity stream) {
        try {
            return zlmClient.getMediaInfo("rtmp", zlmProperties.getVhost(), stream.getLocalApp(), stream.getLocalStream()) != null;
        } catch (ZlmClientException ex) {
            return false;
        }
    }

    private StreamRuntime buildRuntime(StreamDefinition definition,
                                       StreamState state,
                                       String zlmTaskKey,
                                       String proxyKey,
                                       boolean online,
                                       String lastError) {
        return StreamRuntime.builder()
                .streamId(definition.getStreamId())
                .accessMode(definition.getAccessMode())
                .state(state)
                .zlmTaskKey(zlmTaskKey)
                .proxyKey(proxyKey)
                .localOnline(online)
                .cloudOnline(false)
                .lastError(lastError)
                .updatedAt(Instant.now())
                .build();
    }

    private String buildLocalRtmpUrl(StreamDefinition definition) {
        URI apiUri = URI.create(zlmProperties.getApiUrl());
        String host = apiUri.getHost() == null ? "127.0.0.1" : apiUri.getHost();
        return "rtmp://" + host + "/" + definition.getLocalApp() + "/" + definition.getLocalStream();
    }
}