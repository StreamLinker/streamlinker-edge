package io.streamlinker.edge.infra.db.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.streamlinker.edge.infra.db.entity.StreamPushRuntimeEntity;
import io.streamlinker.edge.infra.db.mapper.StreamPushRuntimeMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StreamPushRuntimeRepository {

    private final StreamPushRuntimeMapper streamPushRuntimeMapper;

    public StreamPushRuntimeRepository(StreamPushRuntimeMapper streamPushRuntimeMapper) {
        this.streamPushRuntimeMapper = streamPushRuntimeMapper;
    }

    public StreamPushRuntimeEntity findByPushTargetId(Long pushTargetId) {
        return streamPushRuntimeMapper.selectById(pushTargetId);
    }

    public StreamPushRuntimeEntity saveOrUpdate(StreamPushRuntimeEntity entity) {
        StreamPushRuntimeEntity existing = findByPushTargetId(entity.getPushTargetId());
        if (existing == null) {
            streamPushRuntimeMapper.insert(entity);
            return entity;
        }
        streamPushRuntimeMapper.updateById(entity);
        return entity;
    }

    public List<StreamPushRuntimeEntity> findAll() {
        return streamPushRuntimeMapper.selectList(new LambdaQueryWrapper<>());
    }

    public void deleteAll() {
        streamPushRuntimeMapper.delete(new LambdaQueryWrapper<>());
    }
}