package io.streamlinker.edge.web;

import io.streamlinker.edge.service.PushTargetAdminService;
import io.streamlinker.edge.web.dto.PushTargetUpsertRequest;
import io.streamlinker.edge.web.dto.PushTargetView;
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
@RequestMapping("/api/admin/push-targets")
public class PushTargetAdminController {

    private final PushTargetAdminService pushTargetAdminService;

    public PushTargetAdminController(PushTargetAdminService pushTargetAdminService) {
        this.pushTargetAdminService = pushTargetAdminService;
    }

    @GetMapping
    public List<PushTargetView> list() {
        return pushTargetAdminService.list();
    }

    @GetMapping("/by-stream/{streamCode}")
    public List<PushTargetView> listByStream(@PathVariable String streamCode) {
        return pushTargetAdminService.listByStreamCode(streamCode);
    }

    @GetMapping("/{targetCode}")
    public PushTargetView get(@PathVariable String targetCode) {
        return pushTargetAdminService.get(targetCode);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PushTargetView create(@Valid @RequestBody PushTargetUpsertRequest request) {
        return pushTargetAdminService.create(request);
    }

    @PutMapping("/{targetCode}")
    public PushTargetView update(@PathVariable String targetCode, @Valid @RequestBody PushTargetUpsertRequest request) {
        return pushTargetAdminService.update(targetCode, request);
    }

    @DeleteMapping("/{targetCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String targetCode) {
        pushTargetAdminService.delete(targetCode);
    }
}