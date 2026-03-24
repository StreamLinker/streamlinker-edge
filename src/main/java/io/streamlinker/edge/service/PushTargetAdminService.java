package io.streamlinker.edge.service;

import io.streamlinker.edge.infra.db.entity.StreamEntity;
import io.streamlinker.edge.infra.db.entity.StreamPushTargetEntity;
import io.streamlinker.edge.infra.db.repository.StreamPushTargetRepository;
import io.streamlinker.edge.infra.db.repository.StreamRepository;
import io.streamlinker.edge.web.dto.PushTargetUpsertRequest;
import io.streamlinker.edge.web.dto.PushTargetView;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PushTargetAdminService {

    private final StreamRepository streamRepository;
    private final StreamPushTargetRepository streamPushTargetRepository;
    private final AtomicLong idGenerator;

    public PushTargetAdminService(StreamRepository streamRepository,
                                  StreamPushTargetRepository streamPushTargetRepository) {
        this.streamRepository = streamRepository;
        this.streamPushTargetRepository = streamPushTargetRepository;
        long seed = streamPushTargetRepository.findAll().stream()
                .map(StreamPushTargetEntity::getId)
                .filter(id -> id != null)
                .max(Comparator.naturalOrder())
                .orElse(0L);
        this.idGenerator = new AtomicLong(seed + 1);
    }

    public List<PushTargetView> list() {
        return streamPushTargetRepository.findAll().stream()
                .filter(entity -> entity.getDeleted() == null || entity.getDeleted() == 0)
                .map(this::toView)
                .toList();
    }

    public List<PushTargetView> listByStreamCode(String streamCode) {
        StreamEntity stream = requireStream(streamCode);
        return streamPushTargetRepository.findEnabledByStreamId(stream.getId()).stream()
                .map(this::toView)
                .toList();
    }

    public PushTargetView get(String targetCode) {
        return toView(requireTarget(targetCode));
    }

    public PushTargetView create(PushTargetUpsertRequest request) {
        if (streamPushTargetRepository.findByTargetCode(request.getTargetCode()) != null) {
            throw new IllegalArgumentException("Duplicate targetCode: " + request.getTargetCode());
        }
        requireStream(request.getStreamId());
        StreamPushTargetEntity entity = new StreamPushTargetEntity();
        entity.setId(idGenerator.getAndIncrement());
        apply(entity, request);
        LocalDateTime now = LocalDateTime.now();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setDeleted(0);
        return toView(streamPushTargetRepository.save(entity));
    }

    public PushTargetView update(String targetCode, PushTargetUpsertRequest request) {
        StreamPushTargetEntity entity = requireTarget(targetCode);
        requireStream(request.getStreamId());
        apply(entity, request);
        entity.setUpdateTime(LocalDateTime.now());
        return toView(streamPushTargetRepository.save(entity));
    }

    public void delete(String targetCode) {
        StreamPushTargetEntity entity = requireTarget(targetCode);
        entity.setDeleted(1);
        entity.setUpdateTime(LocalDateTime.now());
        streamPushTargetRepository.save(entity);
    }

    private StreamEntity requireStream(String streamCode) {
        StreamEntity entity = streamRepository.findByStreamId(streamCode);
        if (entity == null || (entity.getDeleted() != null && entity.getDeleted() == 1)) {
            throw new IllegalArgumentException("Unknown streamCode: " + streamCode);
        }
        return entity;
    }

    private StreamEntity requireStream(Long streamId) {
        return streamRepository.findAll().stream()
                .filter(entity -> streamId.equals(entity.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown streamId: " + streamId));
    }

    private StreamPushTargetEntity requireTarget(String targetCode) {
        StreamPushTargetEntity entity = streamPushTargetRepository.findByTargetCode(targetCode);
        if (entity == null || (entity.getDeleted() != null && entity.getDeleted() == 1)) {
            throw new IllegalArgumentException("Unknown targetCode: " + targetCode);
        }
        return entity;
    }

    private void apply(StreamPushTargetEntity entity, PushTargetUpsertRequest request) {
        entity.setStreamId(request.getStreamId());
        entity.setTargetCode(request.getTargetCode());
        entity.setTargetName(request.getTargetName());
        entity.setTargetType(request.getTargetType());
        entity.setTargetProtocol(request.getTargetProtocol());
        entity.setTargetUrl(request.getTargetUrl());
        entity.setTargetApp(request.getTargetApp());
        entity.setTargetStream(request.getTargetStream());
        entity.setEnabled(request.getEnabled());
        entity.setExpectedState(request.getExpectedState());
        entity.setRemark(request.getRemark());
    }

    private PushTargetView toView(StreamPushTargetEntity entity) {
        return PushTargetView.builder()
                .id(entity.getId())
                .streamId(entity.getStreamId())
                .targetCode(entity.getTargetCode())
                .targetName(entity.getTargetName())
                .targetType(entity.getTargetType())
                .targetProtocol(entity.getTargetProtocol())
                .targetUrl(entity.getTargetUrl())
                .targetApp(entity.getTargetApp())
                .targetStream(entity.getTargetStream())
                .enabled(entity.getEnabled())
                .expectedState(entity.getExpectedState())
                .remark(entity.getRemark())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}