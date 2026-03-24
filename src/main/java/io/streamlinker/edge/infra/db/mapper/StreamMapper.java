package io.streamlinker.edge.infra.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.streamlinker.edge.infra.db.entity.StreamEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StreamMapper extends BaseMapper<StreamEntity> {

    @Delete("DELETE FROM stream")
    void hardDeleteAll();
}