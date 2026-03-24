package io.streamlinker.edge.service;

import io.streamlinker.edge.domain.AccessMode;
import io.streamlinker.edge.domain.StreamDefinition;
import io.streamlinker.edge.domain.StreamRuntime;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PullHandlerContractTest {

    @Test
    void ffmpegHandlerShouldOnlyProducePullSideState() {
        LocalPullExecutionRegistry registry = new LocalPullExecutionRegistry();
        PullExecutionGateway gateway = new SimulatedPullExecutionGateway(registry);
        FfmpegPullPushHandler handler = new FfmpegPullPushHandler(gateway);
        StreamRuntime runtime = handler.start(definition(AccessMode.FFMPEG, "cam-ffmpeg"), null);

        assertThat(runtime.getZlmTaskKey()).isEqualTo("ffmpeg-cam-ffmpeg");
        assertThat(runtime.getProxyKey()).isNull();
        assertThat(runtime.getPusherKey()).isNull();
        assertThat(registry.isOnline("cam-ffmpeg")).isTrue();
    }

    @Test
    void proxyHandlerShouldOnlyProducePullSideState() {
        LocalPullExecutionRegistry registry = new LocalPullExecutionRegistry();
        PullExecutionGateway gateway = new SimulatedPullExecutionGateway(registry);
        ProxyPullPushHandler handler = new ProxyPullPushHandler(gateway);
        StreamRuntime runtime = handler.start(definition(AccessMode.PROXY, "cam-proxy"), null);

        assertThat(runtime.getProxyKey()).isEqualTo("proxy-cam-proxy");
        assertThat(runtime.getZlmTaskKey()).isNull();
        assertThat(runtime.getPusherKey()).isNull();
        assertThat(registry.isOnline("cam-proxy")).isTrue();
    }

    private StreamDefinition definition(AccessMode mode, String streamId) {
        return StreamDefinition.builder()
                .streamId(streamId)
                .accessMode(mode)
                .localApp("live")
                .localStream(streamId)
                .build();
    }
}