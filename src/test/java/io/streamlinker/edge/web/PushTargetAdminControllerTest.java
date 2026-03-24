package io.streamlinker.edge.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamlinker.edge.infra.db.entity.StreamEntity;
import io.streamlinker.edge.infra.db.entity.StreamPushTargetEntity;
import io.streamlinker.edge.infra.db.repository.StreamPushRuntimeRepository;
import io.streamlinker.edge.infra.db.repository.StreamPushTargetRepository;
import io.streamlinker.edge.infra.db.repository.StreamRepository;
import io.streamlinker.edge.infra.db.repository.StreamRuntimeRepository;
import io.streamlinker.edge.web.dto.PushTargetUpsertRequest;
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
        "spring.datasource.url=jdbc:h2:mem:push_target_admin_controller;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:sql/streamlinker-edge-schema-h2.sql"
})
@AutoConfigureMockMvc
class PushTargetAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StreamRepository streamRepository;

    @Autowired
    private StreamRuntimeRepository streamRuntimeRepository;

    @Autowired
    private StreamPushTargetRepository streamPushTargetRepository;

    @Autowired
    private StreamPushRuntimeRepository streamPushRuntimeRepository;

    @BeforeEach
    void setUp() {
        cleanup();
        streamRepository.save(streamEntity(1L, "cam-admin"));
        streamPushTargetRepository.save(pushTargetEntity(10L, 1L, "push-admin"));
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    @Test
    void shouldCreatePushTarget() throws Exception {
        PushTargetUpsertRequest request = request(1L, "push-new");

        mockMvc.perform(post("/api/admin/push-targets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.targetCode").value("push-new"));
    }

    @Test
    void shouldListGetAndListByStream() throws Exception {
        mockMvc.perform(get("/api/admin/push-targets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].targetCode").value("push-admin"));

        mockMvc.perform(get("/api/admin/push-targets/push-admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetCode").value("push-admin"));

        mockMvc.perform(get("/api/admin/push-targets/by-stream/cam-admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].targetCode").value("push-admin"));
    }

    @Test
    void shouldUpdatePushTarget() throws Exception {
        PushTargetUpsertRequest request = request(1L, "push-admin");
        request.setTargetName("push-admin-updated");

        mockMvc.perform(put("/api/admin/push-targets/push-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetName").value("push-admin-updated"));
    }

    @Test
    void shouldDeletePushTarget() throws Exception {
        mockMvc.perform(delete("/api/admin/push-targets/push-admin"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/admin/push-targets/push-admin"))
                .andExpect(status().isBadRequest());
    }

    private void cleanup() {
        streamPushRuntimeRepository.deleteAll();
        streamRuntimeRepository.deleteAll();
        streamPushTargetRepository.deleteAll();
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

    private StreamPushTargetEntity pushTargetEntity(Long id, Long streamId, String code) {
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
        entity.setExpectedState("STOPPED");
        entity.setDeleted(0);
        return entity;
    }

    private PushTargetUpsertRequest request(Long streamId, String targetCode) {
        PushTargetUpsertRequest request = new PushTargetUpsertRequest();
        request.setStreamId(streamId);
        request.setTargetCode(targetCode);
        request.setTargetName(targetCode);
        request.setTargetType("cloud");
        request.setTargetProtocol("RTMP");
        request.setTargetUrl("rtmp://cloud/live/" + targetCode);
        request.setTargetApp("live");
        request.setTargetStream(targetCode);
        request.setEnabled(1);
        request.setExpectedState("STOPPED");
        return request;
    }
}