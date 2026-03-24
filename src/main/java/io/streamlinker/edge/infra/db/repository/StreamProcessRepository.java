package io.streamlinker.edge.infra.db.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.streamlinker.edge.infra.db.entity.StreamProcessEntity;
import io.streamlinker.edge.infra.db.mapper.StreamProcessMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StreamProcessRepository {

    private static final List<String> ACTIVE_STATUSES = List.of("INIT", "RUNNING");

    private final StreamProcessMapper streamProcessMapper;

    public StreamProcessRepository(StreamProcessMapper streamProcessMapper) {
        this.streamProcessMapper = streamProcessMapper;
    }

    public StreamProcessEntity findById(Long processId) {
        return streamProcessMapper.selectById(processId);
    }

    public StreamProcessEntity findActiveByStreamIdAndType(Long streamId, String processType) {
        return streamProcessMapper.selectOne(new LambdaQueryWrapper<StreamProcessEntity>()
                .eq(StreamProcessEntity::getStreamId, streamId)
                .eq(StreamProcessEntity::getProcessType, processType)
                .in(StreamProcessEntity::getStatus, ACTIVE_STATUSES)
                .last("limit 1"));
    }

    public StreamProcessEntity findActiveByPushTargetIdAndType(Long pushTargetId, String processType) {
        return streamProcessMapper.selectOne(new LambdaQueryWrapper<StreamProcessEntity>()
                .eq(StreamProcessEntity::getPushTargetId, pushTargetId)
                .eq(StreamProcessEntity::getProcessType, processType)
                .in(StreamProcessEntity::getStatus, ACTIVE_STATUSES)
                .last("limit 1"));
    }

    public StreamProcessEntity findLatestByStreamIdAndType(Long streamId, String processType) {
        return streamProcessMapper.selectOne(new LambdaQueryWrapper<StreamProcessEntity>()
                .eq(StreamProcessEntity::getStreamId, streamId)
                .eq(StreamProcessEntity::getProcessType, processType)
                .orderByDesc(StreamProcessEntity::getCreateTime)
                .last("limit 1"));
    }

    public StreamProcessEntity findLatestByPushTargetIdAndType(Long pushTargetId, String processType) {
        return streamProcessMapper.selectOne(new LambdaQueryWrapper<StreamProcessEntity>()
                .eq(StreamProcessEntity::getPushTargetId, pushTargetId)
                .eq(StreamProcessEntity::getProcessType, processType)
                .orderByDesc(StreamProcessEntity::getCreateTime)
                .last("limit 1"));
    }

    public List<StreamProcessEntity> findRecentByStreamId(Long streamId, int limit) {
        return streamProcessMapper.selectList(new LambdaQueryWrapper<StreamProcessEntity>()
                .eq(StreamProcessEntity::getStreamId, streamId)
                .orderByDesc(StreamProcessEntity::getCreateTime)
                .last("limit " + clampLimit(limit)));
    }

    public List<StreamProcessEntity> findRecentByPushTargetId(Long pushTargetId, int limit) {
        return streamProcessMapper.selectList(new LambdaQueryWrapper<StreamProcessEntity>()
                .eq(StreamProcessEntity::getPushTargetId, pushTargetId)
                .orderByDesc(StreamProcessEntity::getCreateTime)
                .last("limit " + clampLimit(limit)));
    }

    public StreamProcessEntity create(StreamProcessEntity entity) {
        streamProcessMapper.insert(entity);
        return entity;
    }

    public StreamProcessEntity save(StreamProcessEntity entity) {
        streamProcessMapper.updateById(entity);
        return entity;
    }

    public void deleteAll() {
        streamProcessMapper.delete(new LambdaQueryWrapper<>());
    }

    private int clampLimit(int limit) {
        return Math.max(1, Math.min(limit, 50));
    }
}