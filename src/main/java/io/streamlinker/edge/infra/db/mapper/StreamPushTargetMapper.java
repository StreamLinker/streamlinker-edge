package io.streamlinker.edge.infra.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.streamlinker.edge.infra.db.entity.StreamPushTargetEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StreamPushTargetMapper extends BaseMapper<StreamPushTargetEntity> {

    @Delete("DELETE FROM stream_push_target")
    void hardDeleteAll();
}