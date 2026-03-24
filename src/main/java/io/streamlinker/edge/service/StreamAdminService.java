package io.streamlinker.edge.service;

import io.streamlinker.edge.infra.db.entity.StreamEntity;
import io.streamlinker.edge.infra.db.repository.StreamRepository;
import io.streamlinker.edge.web.dto.StreamUpsertRequest;
import io.streamlinker.edge.web.dto.StreamView;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class StreamAdminService {

    private final StreamRepository streamRepository;
    private final AtomicLong idGenerator;

    public StreamAdminService(StreamRepository streamRepository) {
        this.streamRepository = streamRepository;
        long seed = streamRepository.findAll().stream()
                .map(StreamEntity::getId)
                .filter(id -> id != null)
                .max(Comparator.naturalOrder())
                .orElse(0L);
        this.idGenerator = new AtomicLong(seed + 1);
    }

    public List<StreamView> list() {
        return streamRepository.findAll().stream()
                .filter(entity -> entity.getDeleted() == null || entity.getDeleted() == 0)
                .map(this::toView)
                .toList();
    }

    public StreamView get(String streamCode) {
        return toView(requireStream(streamCode));
    }

    public StreamView create(StreamUpsertRequest request) {
        if (streamRepository.findByStreamId(request.getStreamCode()) != null) {
            throw new IllegalArgumentException("Duplicate streamCode: " + request.getStreamCode());
        }
        StreamEntity entity = new StreamEntity();
        entity.setId(idGenerator.getAndIncrement());
        apply(entity, request);
        LocalDateTime now = LocalDateTime.now();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setDeleted(0);
        return toView(streamRepository.save(entity));
    }

    public StreamView update(String streamCode, StreamUpsertRequest request) {
        StreamEntity entity = requireStream(streamCode);
        apply(entity, request);
        entity.setUpdateTime(LocalDateTime.now());
        return toView(streamRepository.save(entity));
    }

    public void delete(String streamCode) {
        StreamEntity entity = requireStream(streamCode);
        entity.setDeleted(1);
        entity.setUpdateTime(LocalDateTime.now());
        streamRepository.save(entity);
    }

    private StreamEntity requireStream(String streamCode) {
        StreamEntity entity = streamRepository.findByStreamId(streamCode);
        if (entity == null || (entity.getDeleted() != null && entity.getDeleted() == 1)) {
            throw new IllegalArgumentException("Unknown streamCode: " + streamCode);
        }
        return entity;
    }

    private void apply(StreamEntity entity, StreamUpsertRequest request) {
        entity.setStreamCode(request.getStreamCode());
        entity.setName(request.getName());
        entity.setSourceUrl(request.getSourceUrl());
        entity.setSourceProtocol(request.getSourceProtocol());
        entity.setAccessMode(request.getAccessMode());
        entity.setLocalApp(request.getLocalApp());
        entity.setLocalStream(request.getLocalStream());
        entity.setEnabled(request.getEnabled());
        entity.setExpectedState(request.getExpectedState());
        entity.setRemark(request.getRemark());
    }

    private StreamView toView(StreamEntity entity) {
        return StreamView.builder()
                .id(entity.getId())
                .streamCode(entity.getStreamCode())
                .name(entity.getName())
                .sourceUrl(entity.getSourceUrl())
                .sourceProtocol(entity.getSourceProtocol())
                .accessMode(entity.getAccessMode())
                .localApp(entity.getLocalApp())
                .localStream(entity.getLocalStream())
                .enabled(entity.getEnabled())
                .expectedState(entity.getExpectedState())
                .remark(entity.getRemark())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}