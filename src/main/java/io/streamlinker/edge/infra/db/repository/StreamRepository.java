package io.streamlinker.edge.infra.db.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.streamlinker.edge.infra.db.entity.StreamEntity;
import io.streamlinker.edge.infra.db.mapper.StreamMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StreamRepository {

    private final StreamMapper streamMapper;

    public StreamRepository(StreamMapper streamMapper) {
        this.streamMapper = streamMapper;
    }

    public StreamEntity findByStreamId(String streamCode) {
        return streamMapper.selectOne(new LambdaQueryWrapper<StreamEntity>()
                .eq(StreamEntity::getStreamCode, streamCode)
                .last("limit 1"));
    }

    public List<StreamEntity> findActiveByExpectedState(String expectedState) {
        return streamMapper.selectList(new LambdaQueryWrapper<StreamEntity>()
                .eq(StreamEntity::getEnabled, 1)
                .eq(StreamEntity::getDeleted, 0)
                .eq(StreamEntity::getExpectedState, expectedState));
    }

    public StreamEntity save(StreamEntity entity) {
        if (entity.getId() == null || streamMapper.selectById(entity.getId()) == null) {
            streamMapper.insert(entity);
            return entity;
        }
        streamMapper.updateById(entity);
        return entity;
    }

    public List<StreamEntity> findAll() {
        return streamMapper.selectList(new LambdaQueryWrapper<>());
    }

    public void deleteAll() {
        streamMapper.hardDeleteAll();
    }
}