package io.streamlinker.edge.web;

import io.streamlinker.edge.infra.db.entity.StreamEntity;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:local_push_target_controller;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:sql/streamlinker-edge-schema-h2.sql",
        "streamlinker.edge.media.simulation-enabled=true"
})
@AutoConfigureMockMvc
class LocalPushTargetControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

    @BeforeEach
    void setUp() {
        cleanup();
        streamRepository.save(streamEntity());
        streamPushTargetRepository.save(pushTargetEntity());
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    @Test
    void shouldListPushRuntimeViews() throws Exception {
        mockMvc.perform(get("/api/local/push-targets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].targetCode").value("push-demo"))
                .andExpect(jsonPath("$[0].pushStatus").value("IDLE"));
    }

    @Test
    void shouldStartAndStopPushTarget() throws Exception {
        mockMvc.perform(post("/api/local/push-targets/push-demo/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pushStatus").value("RUNNING"))
                .andExpect(jsonPath("$.online").value(1));

        mockMvc.perform(post("/api/local/push-targets/push-demo/stop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pushStatus").value("IDLE"))
                .andExpect(jsonPath("$.online").value(0));
    }

    private void cleanup() {
        streamProcessRepository.deleteAll();
        streamPushRuntimeRepository.deleteAll();
        streamPushTargetRepository.deleteAll();
        streamRuntimeRepository.deleteAll();
        streamRepository.deleteAll();
    }

    private StreamEntity streamEntity() {
        StreamEntity entity = new StreamEntity();
        entity.setId(1L);
        entity.setStreamCode("cam-ui");
        entity.setName("cam-ui");
        entity.setSourceUrl("rtsp://example/cam-ui");
        entity.setSourceProtocol("RTSP");
        entity.setAccessMode("FFMPEG");
        entity.setLocalApp("live");
        entity.setLocalStream("cam-ui");
        entity.setEnabled(1);
        entity.setExpectedState("STOPPED");
        entity.setDeleted(0);
        return entity;
    }

    private StreamPushTargetEntity pushTargetEntity() {
        StreamPushTargetEntity entity = new StreamPushTargetEntity();
        entity.setId(10L);
        entity.setStreamId(1L);
        entity.setTargetCode("push-demo");
        entity.setTargetName("push-demo");
        entity.setTargetType("CLOUD_ZLM");
        entity.setTargetProtocol("RTMP");
        entity.setTargetUrl("rtmp://example/live/cam-ui");
        entity.setTargetApp("live");
        entity.setTargetStream("cam-ui");
        entity.setEnabled(1);
        entity.setExpectedState("STOPPED");
        entity.setDeleted(0);
        return entity;
    }
}