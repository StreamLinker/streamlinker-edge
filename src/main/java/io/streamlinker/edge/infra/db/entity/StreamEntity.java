package io.streamlinker.edge.infra.db.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("stream")
public class StreamEntity {

    @TableId
    private Long id;
    private String streamCode;
    private String name;
    private String sourceUrl;
    private String sourceProtocol;
    private String accessMode;
    private String localApp;
    private String localStream;
    private Integer enabled;
    private String expectedState;
    private Integer deleted;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}