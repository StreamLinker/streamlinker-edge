package io.streamlinker.edge.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamlinker.edge.infra.db.entity.StreamEntity;
import io.streamlinker.edge.infra.db.repository.StreamRepository;
import io.streamlinker.edge.infra.db.repository.StreamRuntimeRepository;
import io.streamlinker.edge.web.dto.StreamUpsertRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:stream_admin_controller;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:sql/streamlinker-edge-schema-h2.sql"
})
@AutoConfigureMockMvc
class StreamAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StreamRepository streamRepository;

    @Autowired
    private StreamRuntimeRepository streamRuntimeRepository;

    @BeforeEach
    void setUp() {
        cleanup();
        streamRepository.save(streamEntity(1L, "cam-admin"));
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    @Test
    void shouldCreateStream() throws Exception {
        StreamUpsertRequest request = request("cam-new", "PROXY");

        mockMvc.perform(post("/api/admin/streams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.streamCode").value("cam-new"))
                .andExpect(jsonPath("$.accessMode").value("PROXY"));
    }

    @Test
    void shouldListAndGetStream() throws Exception {
        mockMvc.perform(get("/api/admin/streams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].streamCode").value("cam-admin"));

        mockMvc.perform(get("/api/admin/streams/cam-admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.streamCode").value("cam-admin"));
    }

    @Test
    void shouldUpdateStream() throws Exception {
        StreamUpsertRequest request = request("cam-admin", "PROXY");
        request.setName("cam-admin-updated");

        mockMvc.perform(put("/api/admin/streams/cam-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("cam-admin-updated"))
                .andExpect(jsonPath("$.accessMode").value("PROXY"));
    }

    @Test
    void shouldDeleteStream() throws Exception {
        mockMvc.perform(delete("/api/admin/streams/cam-admin"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/admin/streams/cam-admin"))
                .andExpect(status().isBadRequest());
    }

    private void cleanup() {
        streamRuntimeRepository.deleteAll();
        streamRepository.deleteAll();
    }

    private StreamEntity streamEntity(Long id, String code) {
        StreamEntity entity = new StreamEntity();
        entity.setId(id);
        entity.setStreamCode(code);
        entity.setName(code);
        entity.setSourceUrl("rtsp://example/" + code);
        entity.setSourceProtocol("RTSP");
        entity.setAccessMode("FFMPEG");
        entity.setLocalApp("live");
        entity.setLocalStream(code);
        entity.setEnabled(1);
        entity.setExpectedState("STOPPED");
        entity.setDeleted(0);
        return entity;
    }

    private StreamUpsertRequest request(String streamCode, String accessMode) {
        StreamUpsertRequest request = new StreamUpsertRequest();
        request.setStreamCode(streamCode);
        request.setName(streamCode);
        request.setSourceUrl("rtsp://example/" + streamCode);
        request.setSourceProtocol("RTSP");
        request.setAccessMode(accessMode);
        request.setLocalApp("live");
        request.setLocalStream(streamCode);
        request.setEnabled(1);
        request.setExpectedState("STOPPED");
        return request;
    }
}