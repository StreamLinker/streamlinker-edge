package io.streamlinker.edge.infra.db.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.streamlinker.edge.infra.db.entity.StreamPushTargetEntity;
import io.streamlinker.edge.infra.db.mapper.StreamPushTargetMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StreamPushTargetRepository {

    private final StreamPushTargetMapper streamPushTargetMapper;

    public StreamPushTargetRepository(StreamPushTargetMapper streamPushTargetMapper) {
        this.streamPushTargetMapper = streamPushTargetMapper;
    }

    public List<StreamPushTargetEntity> findEnabledByStreamId(Long streamId) {
        return streamPushTargetMapper.selectList(new LambdaQueryWrapper<StreamPushTargetEntity>()
                .eq(StreamPushTargetEntity::getStreamId, streamId)
                .eq(StreamPushTargetEntity::getEnabled, 1)
                .eq(StreamPushTargetEntity::getDeleted, 0));
    }

    public StreamPushTargetEntity findByPushTargetId(Long pushTargetId) {
        return streamPushTargetMapper.selectById(pushTargetId);
    }

    public List<StreamPushTargetEntity> findActiveByExpectedState(String expectedState) {
        return streamPushTargetMapper.selectList(new LambdaQueryWrapper<StreamPushTargetEntity>()
                .eq(StreamPushTargetEntity::getEnabled, 1)
                .eq(StreamPushTargetEntity::getDeleted, 0)
                .eq(StreamPushTargetEntity::getExpectedState, expectedState));
    }

    public StreamPushTargetEntity findByTargetCode(String targetCode) {
        return streamPushTargetMapper.selectOne(new LambdaQueryWrapper<StreamPushTargetEntity>()
                .eq(StreamPushTargetEntity::getTargetCode, targetCode)
                .last("limit 1"));
    }

    public StreamPushTargetEntity save(StreamPushTargetEntity entity) {
        if (entity.getId() == null || streamPushTargetMapper.selectById(entity.getId()) == null) {
            streamPushTargetMapper.insert(entity);
            return entity;
        }
        streamPushTargetMapper.updateById(entity);
        return entity;
    }

    public List<StreamPushTargetEntity> findAll() {
        return streamPushTargetMapper.selectList(new LambdaQueryWrapper<>());
    }

    public void deleteAll() {
        streamPushTargetMapper.hardDeleteAll();
    }
}