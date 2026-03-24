package io.streamlinker.edge.service;

import io.streamlinker.edge.domain.AccessMode;
import io.streamlinker.edge.domain.StreamDefinition;
import io.streamlinker.edge.domain.StreamRuntime;
import io.streamlinker.edge.domain.StreamState;
import io.streamlinker.edge.infra.db.entity.StreamEntity;
import io.streamlinker.zlm.api.ZlmClient;
import io.streamlinker.zlm.api.ZlmClientException;
import io.streamlinker.zlm.model.response.MediaInfoResponse;
import io.streamlinker.zlm.model.response.StreamKeyResponse;
import io.streamlinker.zlm.spring.ZlmProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ZlmPullExecutionGatewayTest {

    private ZlmClient zlmClient;
    private ZlmProperties zlmProperties;
    private ZlmPullExecutionGateway gateway;

    @BeforeEach
    void setUp() {
        zlmClient = mock(ZlmClient.class);
        zlmProperties = new ZlmProperties();
        zlmProperties.setApiUrl("http://127.0.0.1:8000");
        zlmProperties.setVhost("__defaultVhost__");
        gateway = new ZlmPullExecutionGateway(zlmClient, zlmProperties);
    }

    @Test
    void shouldStartFfmpegViaZlmApi() {
        when(zlmClient.addFfmpegSource(anyString(), anyString(), anyInt(), anyBoolean(), anyBoolean(), isNull()))
                .thenReturn(StreamKeyResponse.builder().key("ffmpeg-key").build());

        StreamRuntime runtime = gateway.startFfmpeg(definition(AccessMode.FFMPEG, "cam-ffmpeg"), null);

        assertThat(runtime.getState()).isEqualTo(StreamState.RUNNING);
        assertThat(runtime.getZlmTaskKey()).isEqualTo("ffmpeg-key");
        verify(zlmClient).addFfmpegSource(
                "rtsp://example/cam-ffmpeg",
                "rtmp://127.0.0.1/live/cam-ffmpeg",
                15000,
                false,
                false,
                null);
    }

    @Test
    void shouldStartProxyViaZlmApi() {
        when(zlmClient.addStreamProxy(anyString(), anyString(), anyString(), anyInt(), anyBoolean(), anyBoolean()))
                .thenReturn(StreamKeyResponse.builder().key("proxy-key").build());

        StreamRuntime runtime = gateway.startProxy(definition(AccessMode.PROXY, "cam-proxy"), null);

        assertThat(runtime.getState()).isEqualTo(StreamState.RUNNING);
        assertThat(runtime.getProxyKey()).isEqualTo("proxy-key");
        verify(zlmClient).addStreamProxy("live", "cam-proxy", "rtsp://example/cam-proxy", 0, false, true);
    }

    @Test
    void shouldStopFfmpegByTaskKey() {
        StreamRuntime current = StreamRuntime.builder()
                .streamId("cam-ffmpeg")
                .accessMode(AccessMode.FFMPEG)
                .state(StreamState.RUNNING)
                .zlmTaskKey("ffmpeg-key")
                .build();

        StreamRuntime runtime = gateway.stop(definition(AccessMode.FFMPEG, "cam-ffmpeg"), current);

        assertThat(runtime.getState()).isEqualTo(StreamState.IDLE);
        verify(zlmClient).delFfmpegSource("ffmpeg-key");
    }

    @Test
    void shouldStopProxyByTaskKey() {
        StreamRuntime current = StreamRuntime.builder()
                .streamId("cam-proxy")
                .accessMode(AccessMode.PROXY)
                .state(StreamState.RUNNING)
                .proxyKey("proxy-key")
                .build();

        StreamRuntime runtime = gateway.stop(definition(AccessMode.PROXY, "cam-proxy"), current);

        assertThat(runtime.getState()).isEqualTo(StreamState.IDLE);
        verify(zlmClient).delStreamProxy("proxy-key");
    }

    @Test
    void shouldProbeOnlineByMediaInfo() {
        when(zlmClient.getMediaInfo("rtmp", "__defaultVhost__", "live", "cam-probe"))
                .thenReturn(MediaInfoResponse.builder().app("live").stream("cam-probe").schema("rtmp").build());

        assertThat(gateway.isOnline(streamEntity("cam-probe"))).isTrue();
    }

    @Test
    void shouldReturnOfflineWhenMediaInfoFails() {
        when(zlmClient.getMediaInfo("rtmp", "__defaultVhost__", "live", "cam-probe"))
                .thenThrow(new ZlmClientException("offline"));

        assertThat(gateway.isOnline(streamEntity("cam-probe"))).isFalse();
    }

    private StreamDefinition definition(AccessMode mode, String streamId) {
        return StreamDefinition.builder()
                .streamId(streamId)
                .accessMode(mode)
                .sourceUrl("rtsp://example/" + streamId)
                .localApp("live")
                .localStream(streamId)
                .build();
    }

    private StreamEntity streamEntity(String streamId) {
        StreamEntity entity = new StreamEntity();
        entity.setStreamCode(streamId);
        entity.setLocalApp("live");
        entity.setLocalStream(streamId);
        return entity;
    }
}