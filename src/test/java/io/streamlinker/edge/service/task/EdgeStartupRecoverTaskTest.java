package io.streamlinker.edge.service.task;

import io.streamlinker.edge.infra.db.entity.StreamEntity;
import io.streamlinker.edge.infra.db.repository.StreamProcessRepository;
import io.streamlinker.edge.infra.db.repository.StreamRepository;
import io.streamlinker.edge.infra.db.repository.StreamRuntimeRepository;
import io.streamlinker.edge.service.LocalPullExecutionRegistry;
import io.streamlinker.edge.service.StreamOrchestrator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:startup_recover;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:sql/streamlinker-edge-schema-h2.sql",
        "streamlinker.edge.reconcile.startup-recover-enabled=true",
        "streamlinker.edge.reconcile.pull-reconcile-enabled=false",
        "streamlinker.edge.reconcile.runtime-sync-enabled=false"
})
class EdgeStartupRecoverTaskTest {

    @Autowired
    private EdgeStartupRecoverTask edgeStartupRecoverTask;

    @Autowired
    private LocalPullExecutionRegistry executionRegistry;

    @Autowired
    private StreamRepository streamRepository;

    @Autowired
    private StreamRuntimeRepository streamRuntimeRepository;

    @Autowired
    private StreamProcessRepository streamProcessRepository;

    @Autowired
    private StreamOrchestrator streamOrchestrator;

    @BeforeEach
    void setUp() {
        cleanup();
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    @Test
    void shouldRecoverExpectedRunningStreamWhenRuntimeMissing() {
        streamRepository.save(streamEntity(1L, "cam-recover", "FFMPEG", "RUNNING"));

        edgeStartupRecoverTask.recoverExpectedRunningStreams();

        assertEquals("RUNNING", streamRepository.findByStreamId("cam-recover").getExpectedState());
        assertEquals("RUNNING", streamRuntimeRepository.findByStreamId(1L).getPullStatus());
        assertEquals(0, streamProcessRepository.findLatestByStreamIdAndType(1L, "PULL_UP").getRetryCount());
    }

    @Test
    void shouldSkipAlreadyOnlineRuntime() {
        streamRepository.save(streamEntity(2L, "cam-online", "FFMPEG", "RUNNING"));
        streamOrchestrator.start("cam-online");
        streamProcessRepository.deleteAll();

        edgeStartupRecoverTask.recoverExpectedRunningStreams();

        assertNull(streamProcessRepository.findLatestByStreamIdAndType(2L, "PULL_UP"));
    }

    private void cleanup() {
        executionRegistry.clear();
        streamProcessRepository.deleteAll();
        streamRuntimeRepository.deleteAll();
        streamRepository.deleteAll();
    }

    private StreamEntity streamEntity(Long id, String code, String accessMode, String expectedState) {
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
        entity.setExpectedState(expectedState);
        entity.setDeleted(0);
        return entity;
    }
}