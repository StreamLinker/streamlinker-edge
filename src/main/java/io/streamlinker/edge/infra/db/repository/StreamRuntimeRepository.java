package io.streamlinker.edge.infra.db.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.streamlinker.edge.infra.db.entity.StreamRuntimeEntity;
import io.streamlinker.edge.infra.db.mapper.StreamRuntimeMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StreamRuntimeRepository {

    private final StreamRuntimeMapper streamRuntimeMapper;

    public StreamRuntimeRepository(StreamRuntimeMapper streamRuntimeMapper) {
        this.streamRuntimeMapper = streamRuntimeMapper;
    }

    public StreamRuntimeEntity findByStreamId(Long streamId) {
        return streamRuntimeMapper.selectById(streamId);
    }

    public StreamRuntimeEntity saveOrUpdate(StreamRuntimeEntity entity) {
        StreamRuntimeEntity existing = findByStreamId(entity.getStreamId());
        if (existing == null) {
            streamRuntimeMapper.insert(entity);
            return entity;
        }
        streamRuntimeMapper.updateById(entity);
        return entity;
    }

    public List<StreamRuntimeEntity> findAll() {
        return streamRuntimeMapper.selectList(new LambdaQueryWrapper<>());
    }

    public void deleteAll() {
        streamRuntimeMapper.delete(new LambdaQueryWrapper<>());
    }
}