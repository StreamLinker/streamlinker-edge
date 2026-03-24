package io.streamlinker.edge.web;

import io.streamlinker.edge.service.StreamAdminService;
import io.streamlinker.edge.web.dto.StreamUpsertRequest;
import io.streamlinker.edge.web.dto.StreamView;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/streams")
public class StreamAdminController {

    private final StreamAdminService streamAdminService;

    public StreamAdminController(StreamAdminService streamAdminService) {
        this.streamAdminService = streamAdminService;
    }

    @GetMapping
    public List<StreamView> list() {
        return streamAdminService.list();
    }

    @GetMapping("/{streamCode}")
    public StreamView get(@PathVariable String streamCode) {
        return streamAdminService.get(streamCode);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StreamView create(@Valid @RequestBody StreamUpsertRequest request) {
        return streamAdminService.create(request);
    }

    @PutMapping("/{streamCode}")
    public StreamView update(@PathVariable String streamCode, @Valid @RequestBody StreamUpsertRequest request) {
        return streamAdminService.update(streamCode, request);
    }

    @DeleteMapping("/{streamCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String streamCode) {
        streamAdminService.delete(streamCode);
    }
}