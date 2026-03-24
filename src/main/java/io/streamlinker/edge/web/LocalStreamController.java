package io.streamlinker.edge.web;

import io.streamlinker.edge.domain.StreamRuntime;
import io.streamlinker.edge.service.StreamOrchestrator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/local/streams")
public class LocalStreamController {

    private final StreamOrchestrator streamOrchestrator;

    public LocalStreamController(StreamOrchestrator streamOrchestrator) {
        this.streamOrchestrator = streamOrchestrator;
    }

    @GetMapping
    public List<StreamRuntime> list() {
        return streamOrchestrator.list();
    }

    @PostMapping("/{streamId}/start")
    public StreamRuntime start(@PathVariable String streamId) {
        return streamOrchestrator.start(streamId);
    }

    @PostMapping("/{streamId}/stop")
    public StreamRuntime stop(@PathVariable String streamId) {
        return streamOrchestrator.stop(streamId);
    }

    @PostMapping("/{streamId}/restart")
    public StreamRuntime restart(@PathVariable String streamId) {
        return streamOrchestrator.restart(streamId);
    }
}
