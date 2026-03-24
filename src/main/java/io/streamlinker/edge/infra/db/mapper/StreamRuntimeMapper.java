package io.streamlinker.edge.infra.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.streamlinker.edge.infra.db.entity.StreamRuntimeEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StreamRuntimeMapper extends BaseMapper<StreamRuntimeEntity> {
}