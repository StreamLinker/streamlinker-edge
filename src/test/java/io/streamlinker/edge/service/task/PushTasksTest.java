package io.streamlinker.edge.service.task;

import io.streamlinker.edge.infra.db.entity.StreamEntity;
import io.streamlinker.edge.infra.db.entity.StreamPushTargetEntity;
import io.streamlinker.edge.infra.db.repository.StreamProcessRepository;
import io.streamlinker.edge.infra.db.repository.StreamPushRuntimeRepository;
import io.streamlinker.edge.infra.db.repository.StreamPushTargetRepository;
import io.streamlinker.edge.infra.db.repository.StreamRepository;
import io.streamlinker.edge.infra.db.repository.StreamRuntimeRepository;
import io.streamlinker.edge.service.LocalPullExecutionRegistry;
import io.streamlinker.edge.service.LocalPushExecutionRegistry;
import io.streamlinker.edge.service.PushTargetOrchestrator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:push_tasks;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:sql/streamlinker-edge-schema-h2.sql",
        "streamlinker.edge.reconcile.startup-recover-enabled=false",
        "streamlinker.edge.reconcile.pull-reconcile-enabled=false",
        "streamlinker.edge.reconcile.push-reconcile-enabled=true",
        "streamlinker.edge.reconcile.runtime-sync-enabled=true",
        "spring.task.scheduling.enabled=false"
})
class PushTasksTest {

    @Autowired
    private PushTargetOrchestrator pushTargetOrchestrator;

    @Autowired
    private PushRuntimeSyncTask pushRuntimeSyncTask;

    @Autowired
    private PushTargetReconcileTask pushTargetReconcileTask;

    @Autowired
    private StreamRepository streamRepository;

    @Autowired
    private StreamRuntimeRepository streamRuntimeRepository;

    @Autowired
    private StreamPushTargetRepository streamPushTargetRepository;

    @Autowired
    private StreamPushRuntimeRepository streamPushRuntimeRepository;

    @Autowired
    private StreamProcessRepository streamProcessRepository;

    @Autowired
    private LocalPullExecutionRegistry localPullExecutionRegistry;

    @Autowired
    private LocalPushExecutionRegistry localPushExecutionRegistry;

    @BeforeEach
    void setUp() {
        cleanup();
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    @Test
    void shouldMarkPushRuntimeFailedWhenExpectedRunningButProbeOffline() {
        streamRepository.save(streamEntity(1L, "cam-runtime-push", "FFMPEG", "RUNNING"));
        streamPushTargetRepository.save(pushTargetEntity(21L, 1L, "push-runtime", "RUNNING"));
        pushTargetOrchestrator.start("push-runtime");
        localPushExecutionRegistry.clear();

        pushRuntimeSyncTask.syncAll();

        assertEquals("FAILED", streamPushRuntimeRepository.findByPushTargetId(21L).getPushStatus());
        assertEquals(0, streamPushRuntimeRepository.findByPushTargetId(21L).getOnline());
    }

    @Test
    void shouldReconcileExpectedRunningAndAutoRecoverPullThenPush() {
        streamRepository.save(streamEntity(2L, "cam-recover-push", "FFMPEG", "RUNNING"));
        streamPushTargetRepository.save(pushTargetEntity(22L, 2L, "push-recover", "RUNNING"));

        pushTargetReconcileTask.reconcileExpectedRunning();

        assertEquals("RUNNING", streamRuntimeRepository.findByStreamId(2L).getPullStatus());
        assertEquals("RUNNING", streamPushRuntimeRepository.findByPushTargetId(22L).getPushStatus());
        assertTrue(localPullExecutionRegistry.isOnline("cam-recover-push"));
        assertTrue(localPushExecutionRegistry.isOnline("push-recover"));
    }

    private void cleanup() {
        localPullExecutionRegistry.clear();
        localPushExecutionRegistry.clear();
        streamProcessRepository.deleteAll();
        streamPushRuntimeRepository.deleteAll();
        streamRuntimeRepository.deleteAll();
        streamPushTargetRepository.deleteAll();
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

    private StreamPushTargetEntity pushTargetEntity(Long id, Long streamId, String code, String expectedState) {
        StreamPushTargetEntity entity = new StreamPushTargetEntity();
        entity.setId(id);
        entity.setStreamId(streamId);
        entity.setTargetCode(code);
        entity.setTargetName(code);
        entity.setTargetType("cloud");
        entity.setTargetProtocol("RTMP");
        entity.setTargetUrl("rtmp://cloud/live/" + code);
        entity.setTargetApp("live");
        entity.setTargetStream(code);
        entity.setEnabled(1);
        entity.setExpectedState(expectedState);
        entity.setDeleted(0);
        return entity;
    }
}