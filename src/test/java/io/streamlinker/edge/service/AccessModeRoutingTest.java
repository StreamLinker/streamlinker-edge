package io.streamlinker.edge.service;

import io.streamlinker.edge.domain.StreamRuntime;
import io.streamlinker.edge.domain.StreamState;
import io.streamlinker.edge.infra.db.entity.StreamEntity;
import io.streamlinker.edge.infra.db.repository.StreamRepository;
import io.streamlinker.edge.infra.db.repository.StreamRuntimeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:access_mode_routing;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:sql/streamlinker-edge-schema-h2.sql"
})
class AccessModeRoutingTest {

    @Autowired
    private StreamOrchestrator orchestrator;

    @Autowired
    private StreamRepository streamRepository;

    @Autowired
    private StreamRuntimeRepository streamRuntimeRepository;

    @BeforeEach
    void setUp() {
        cleanup();
        streamRepository.save(streamEntity(1L, "cam-ffmpeg", "FFMPEG"));
        streamRepository.save(streamEntity(2L, "cam-proxy", "PROXY"));
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    @Test
    void shouldRouteFfmpegModeToFfmpegHandler() {
        StreamRuntime runtime = orchestrator.start("cam-ffmpeg");

        assertEquals(StreamState.RUNNING, runtime.getState());
        assertNotNull(runtime.getZlmTaskKey());
        assertNull(runtime.getProxyKey());
    }

    @Test
    void shouldRouteProxyModeToProxyHandler() {
        StreamRuntime runtime = orchestrator.start("cam-proxy");

        assertEquals(StreamState.RUNNING, runtime.getState());
        assertNotNull(runtime.getProxyKey());
        assertNull(runtime.getPusherKey());
    }

    private void cleanup() {
        streamRuntimeRepository.deleteAll();
        streamRepository.deleteAll();
    }

    private StreamEntity streamEntity(Long id, String code, String accessMode) {
        StreamEntity entity = new StreamEntity();
        entity.setId(id);
        entity.setStreamCode(code);
        entity.setName(code);
        entity.setSourceUrl("rtsp://example/" + code);
        entity.setSourceProtocol("RTSP");
        entity.setAccessMode(accessMode);
        entity.setLocalApp("live");
        entity.setLocalStream(code);
        entity.setEnabled(1);
        entity.setExpectedState("STOPPED");
        entity.setDeleted(0);
        return entity;
    }
}