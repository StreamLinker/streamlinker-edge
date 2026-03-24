package io.streamlinker.edge.web;

import io.streamlinker.edge.infra.db.entity.StreamEntity;
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
        "spring.datasource.url=jdbc:h2:mem:local_stream_controller;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:sql/streamlinker-edge-schema-h2.sql"
})
@AutoConfigureMockMvc
class LocalStreamControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    void shouldListStreams() throws Exception {
        mockMvc.perform(post("/api/local/streams/cam-ffmpeg/start")).andExpect(status().isOk());
        mockMvc.perform(get("/api/local/streams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].streamId").exists());
    }

    @Test
    void shouldStartStream() throws Exception {
        mockMvc.perform(post("/api/local/streams/cam-ffmpeg/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("RUNNING"));
    }

    @Test
    void shouldStopStream() throws Exception {
        mockMvc.perform(post("/api/local/streams/cam-ffmpeg/start")).andExpect(status().isOk());
        mockMvc.perform(post("/api/local/streams/cam-ffmpeg/stop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("IDLE"));
    }

    @Test
    void shouldRestartStream() throws Exception {
        mockMvc.perform(post("/api/local/streams/cam-proxy/restart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("RUNNING"));
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