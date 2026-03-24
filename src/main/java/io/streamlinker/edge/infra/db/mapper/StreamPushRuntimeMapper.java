package io.streamlinker.edge.infra.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.streamlinker.edge.infra.db.entity.StreamPushRuntimeEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StreamPushRuntimeMapper extends BaseMapper<StreamPushRuntimeEntity> {
}