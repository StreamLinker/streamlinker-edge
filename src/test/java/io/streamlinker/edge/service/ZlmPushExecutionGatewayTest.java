package io.streamlinker.edge.service;

import io.streamlinker.edge.infra.db.entity.StreamEntity;
import io.streamlinker.edge.infra.db.entity.StreamPushRuntimeEntity;
import io.streamlinker.edge.infra.db.entity.StreamPushTargetEntity;
import io.streamlinker.zlm.api.ZlmClient;
import io.streamlinker.zlm.model.response.StreamKeyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ZlmPushExecutionGatewayTest {

    private ZlmClient zlmClient;
    private LocalPushExecutionRegistry registry;
    private ZlmPushExecutionGateway gateway;

    @BeforeEach
    void setUp() {
        zlmClient = mock(ZlmClient.class);
        registry = new LocalPushExecutionRegistry();
        gateway = new ZlmPushExecutionGateway(zlmClient, registry);
    }

    @Test
    void shouldStartPushViaZlmApi() {
        when(zlmClient.addStreamPusherProxy(anyString(), anyString(), anyString(), anyString(), anyInt()))
                .thenReturn(StreamKeyResponse.builder().key("push-key").build());

        String key = gateway.startPush(streamEntity("cam-push"), targetEntity("push-main"));

        assertThat(key).isEqualTo("push-key");
        assertThat(registry.isOnline("push-main")).isTrue();
        verify(zlmClient).addStreamPusherProxy("live", "cam-push", "rtmp", "rtmp://cloud/live/push-main", 0);
    }

    @Test
    void shouldStopPushViaZlmApi() {
        StreamPushRuntimeEntity runtime = new StreamPushRuntimeEntity();
        runtime.setPusherKey("push-key");
        registry.markRunning("push-main", "push-key");

        gateway.stopPush(targetEntity("push-main"), runtime);

        assertThat(registry.isOnline("push-main")).isFalse();
        verify(zlmClient).delStreamPusherProxy("push-key");
    }

    @Test
    void shouldProbeUsingRegistryState() {
        registry.markRunning("push-main", "push-key");

        assertThat(gateway.isOnline(targetEntity("push-main"))).isTrue();
    }

    private StreamEntity streamEntity(String streamCode) {
        StreamEntity entity = new StreamEntity();
        entity.setStreamCode(streamCode);
        entity.setLocalApp("live");
        entity.setLocalStream(streamCode);
        return entity;
    }

    private StreamPushTargetEntity targetEntity(String targetCode) {
        StreamPushTargetEntity entity = new StreamPushTargetEntity();
        entity.setTargetCode(targetCode);
        entity.setTargetUrl("rtmp://cloud/live/" + targetCode);
        return entity;
    }
}