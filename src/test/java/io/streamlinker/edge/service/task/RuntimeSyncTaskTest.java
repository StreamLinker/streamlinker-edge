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

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:runtime_sync;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:sql/streamlinker-edge-schema-h2.sql",
        "streamlinker.edge.reconcile.startup-recover-enabled=false",
        "streamlinker.edge.reconcile.pull-reconcile-enabled=false",
        "streamlinker.edge.reconcile.runtime-sync-enabled=true"
})
class RuntimeSyncTaskTest {

    @Autowired
    private RuntimeSyncTask runtimeSyncTask;

    @Autowired
    private StreamOrchestrator streamOrchestrator;

    @Autowired
    private LocalPullExecutionRegistry executionRegistry;

    @Autowired
    private StreamRepository streamRepository;

    @Autowired
    private StreamRuntimeRepository streamRuntimeRepository;

    @Autowired
    private StreamProcessRepository streamProcessRepository;

    @BeforeEach
    void setUp() {
        cleanup();
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    @Test
    void shouldMarkRuntimeFailedWhenExpectedRunningButProbeOffline() {
        streamRepository.save(streamEntity(1L, "cam-runtime", "FFMPEG", "RUNNING"));
        streamOrchestrator.start("cam-runtime");
        executionRegistry.clear();

        runtimeSyncTask.syncAll();

        assertEquals("FAILED", streamRuntimeRepository.findByStreamId(1L).getPullStatus());
        assertEquals(0, streamRuntimeRepository.findByStreamId(1L).getLocalOnline());
    }

    @Test
    void shouldKeepRuntimeRunningWhenProbeOnline() {
        streamRepository.save(streamEntity(2L, "cam-running", "FFMPEG", "RUNNING"));
        streamOrchestrator.start("cam-running");

        runtimeSyncTask.syncAll();

        assertEquals("RUNNING", streamRuntimeRepository.findByStreamId(2L).getPullStatus());
        assertEquals(1, streamRuntimeRepository.findByStreamId(2L).getLocalOnline());
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