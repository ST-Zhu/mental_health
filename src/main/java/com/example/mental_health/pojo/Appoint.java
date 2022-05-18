package com.example.mental_health.pojo;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@TableName(value ="tb_appoint")
@Data
public class Appoint implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    private String doctorUsername;

    /**
     * 
     */
    private String userUsername;

    /**
     * 
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 
     */
    private Date createTime;

    /**
     *
     */
    private Date completeTime;

    /**
     * 
     */
    private String state;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}