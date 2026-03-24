package io.streamlinker.edge.service;

import io.streamlinker.edge.domain.ProcessType;
import io.streamlinker.edge.infra.db.entity.StreamEntity;
import io.streamlinker.edge.infra.db.entity.StreamProcessEntity;
import io.streamlinker.edge.infra.db.entity.StreamPushRuntimeEntity;
import io.streamlinker.edge.infra.db.entity.StreamPushTargetEntity;
import io.streamlinker.edge.infra.db.repository.StreamProcessRepository;
import io.streamlinker.edge.infra.db.repository.StreamPushRuntimeRepository;
import io.streamlinker.edge.infra.db.repository.StreamPushTargetRepository;
import io.streamlinker.edge.infra.db.repository.StreamRepository;
import io.streamlinker.edge.infra.db.repository.StreamRuntimeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:push_orchestrator;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:sql/streamlinker-edge-schema-h2.sql",
        "streamlinker.edge.reconcile.startup-recover-enabled=false",
        "streamlinker.edge.reconcile.pull-reconcile-enabled=false",
        "streamlinker.edge.reconcile.push-reconcile-enabled=false",
        "streamlinker.edge.reconcile.runtime-sync-enabled=false",
        "spring.task.scheduling.enabled=false"
})
class PushTargetOrchestratorTest {

    @Autowired
    private PushTargetOrchestrator pushTargetOrchestrator;

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
        streamRepository.save(streamEntity(1L, "cam-push", "FFMPEG", "STOPPED"));
        streamPushTargetRepository.save(pushTargetEntity(11L, 1L, "push-main", "STOPPED", "RTMP"));
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    @Test
    void shouldStartPushAndAutoStartParentPull() {
        StreamPushRuntimeEntity runtime = pushTargetOrchestrator.start("push-main");

        assertEquals("RUNNING", runtime.getPushStatus());
        assertEquals(1, runtime.getOnline());
        assertEquals("RUNNING", streamRepository.findByStreamId("cam-push").getExpectedState());
        assertEquals("RUNNING", streamPushTargetRepository.findByTargetCode("push-main").getExpectedState());
        assertEquals("RUNNING", streamRuntimeRepository.findByStreamId(1L).getPullStatus());
        assertTrue(localPullExecutionRegistry.isOnline("cam-push"));
        assertTrue(localPushExecutionRegistry.isOnline("push-main"));

        StreamProcessEntity pullProcess = streamProcessRepository.findLatestByStreamIdAndType(1L, ProcessType.PULL_UP.name());
        StreamProcessEntity pushProcess = streamProcessRepository.findLatestByPushTargetIdAndType(11L, ProcessType.PUSH_UP.name());
        assertEquals("SUCCESS", pullProcess.getStatus());
        assertEquals("SUCCESS", pushProcess.getStatus());
    }

    @Test
    void shouldStopPush() {
        pushTargetOrchestrator.start("push-main");

        StreamPushRuntimeEntity runtime = pushTargetOrchestrator.stop("push-main");

        assertEquals("IDLE", runtime.getPushStatus());
        assertEquals(0, runtime.getOnline());
        assertEquals("STOPPED", streamPushTargetRepository.findByTargetCode("push-main").getExpectedState());
        StreamProcessEntity process = streamProcessRepository.findLatestByPushTargetIdAndType(11L, ProcessType.PUSH_DOWN.name());
        assertEquals("SUCCESS", process.getStatus());
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

    private StreamPushTargetEntity pushTargetEntity(Long id, Long streamId, String code, String expectedState, String protocol) {
        StreamPushTargetEntity entity = new StreamPushTargetEntity();
        entity.setId(id);
        entity.setStreamId(streamId);
        entity.setTargetCode(code);
        entity.setTargetName(code);
        entity.setTargetType("cloud");
        entity.setTargetProtocol(protocol);
        entity.setTargetUrl("rtmp://cloud/live/" + code);
        entity.setTargetApp("live");
        entity.setTargetStream(code);
        entity.setEnabled(1);
        entity.setExpectedState(expectedState);
        entity.setDeleted(0);
        return entity;
    }
}