package io.streamlinker.edge.service;

import io.streamlinker.edge.domain.ProcessType;
import io.streamlinker.edge.domain.StreamRuntime;
import io.streamlinker.edge.domain.StreamState;
import io.streamlinker.edge.infra.db.entity.StreamEntity;
import io.streamlinker.edge.infra.db.entity.StreamProcessEntity;
import io.streamlinker.edge.infra.db.repository.StreamProcessRepository;
import io.streamlinker.edge.infra.db.repository.StreamRepository;
import io.streamlinker.edge.infra.db.repository.StreamRuntimeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:stream_orchestrator;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:sql/streamlinker-edge-schema-h2.sql",
        "streamlinker.edge.reconcile.startup-recover-enabled=false"
})
class StreamOrchestratorTest {

    @Autowired
    private StreamOrchestrator orchestrator;

    @Autowired
    private StreamRepository streamRepository;

    @Autowired
    private StreamRuntimeRepository streamRuntimeRepository;

    @Autowired
    private StreamProcessRepository streamProcessRepository;

    @BeforeEach
    void setUp() {
        cleanup();
        streamRepository.save(streamEntity(1L, "cam-ffmpeg", "FFMPEG", "STOPPED"));
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    @Test
    void shouldStartStream() {
        StreamRuntime runtime = orchestrator.start("cam-ffmpeg");

        assertEquals(StreamState.RUNNING, runtime.getState());
        assertEquals("RUNNING", streamRepository.findByStreamId("cam-ffmpeg").getExpectedState());
        StreamProcessEntity process = streamProcessRepository.findLatestByStreamIdAndType(1L, ProcessType.PULL_UP.name());
        assertNotNull(process);
        assertEquals("SUCCESS", process.getStatus());
        assertEquals(3, process.getStep());
    }

    @Test
    void shouldStopStream() {
        orchestrator.start("cam-ffmpeg");
        StreamRuntime runtime = orchestrator.stop("cam-ffmpeg");

        assertEquals(StreamState.IDLE, runtime.getState());
        assertEquals("STOPPED", streamRepository.findByStreamId("cam-ffmpeg").getExpectedState());
        StreamProcessEntity process = streamProcessRepository.findLatestByStreamIdAndType(1L, ProcessType.PULL_DOWN.name());
        assertNotNull(process);
        assertEquals("SUCCESS", process.getStatus());
        assertEquals(3, process.getStep());
    }

    @Test
    void shouldBeIdempotentForRepeatedStart() {
        StreamRuntime first = orchestrator.start("cam-ffmpeg");
        StreamRuntime second = orchestrator.start("cam-ffmpeg");

        assertEquals(first, second);
    }

    @Test
    void shouldBeIdempotentForRepeatedStop() {
        StreamRuntime first = orchestrator.stop("cam-ffmpeg");
        StreamRuntime second = orchestrator.stop("cam-ffmpeg");

        assertEquals(first, second);
    }

    private void cleanup() {
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