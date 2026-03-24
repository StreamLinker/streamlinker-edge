package io.streamlinker.edge.web;

import io.streamlinker.edge.infra.db.entity.StreamPushRuntimeEntity;
import io.streamlinker.edge.service.PushTargetOrchestrator;
import io.streamlinker.edge.service.PushTargetRuntimeService;
import io.streamlinker.edge.web.dto.PushTargetRuntimeView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/local/push-targets")
public class LocalPushTargetController {

    private final PushTargetOrchestrator pushTargetOrchestrator;
    private final PushTargetRuntimeService pushTargetRuntimeService;

    public LocalPushTargetController(PushTargetOrchestrator pushTargetOrchestrator,
                                     PushTargetRuntimeService pushTargetRuntimeService) {
        this.pushTargetOrchestrator = pushTargetOrchestrator;
        this.pushTargetRuntimeService = pushTargetRuntimeService;
    }

    @GetMapping
    public List<PushTargetRuntimeView> list() {
        return pushTargetRuntimeService.list();
    }

    @GetMapping("/by-stream/{streamCode}")
    public List<PushTargetRuntimeView> listByStream(@PathVariable String streamCode) {
        return pushTargetRuntimeService.listByStreamCode(streamCode);
    }

    @PostMapping("/{targetCode}/start")
    public StreamPushRuntimeEntity start(@PathVariable String targetCode) {
        return pushTargetOrchestrator.start(targetCode);
    }

    @PostMapping("/{targetCode}/stop")
    public StreamPushRuntimeEntity stop(@PathVariable String targetCode) {
        return pushTargetOrchestrator.stop(targetCode);
    }

    @PostMapping("/{targetCode}/restart")
    public StreamPushRuntimeEntity restart(@PathVariable String targetCode) {
        pushTargetOrchestrator.stop(targetCode);
        return pushTargetOrchestrator.start(targetCode);
    }
}