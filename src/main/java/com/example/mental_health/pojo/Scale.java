package com.example.mental_health.pojo;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 
 * @TableName tb_scale
 */
@TableName(value ="tb_scale")
@Data
public class Scale implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    @TableLogic
    private Integer isDeleted;

    /**
     *
     */
    private String name;

    /**
     * 
     */
    private String depict;

    /**
     * 
     */
    private String notice;

    /**
     * 
     */
    private String standard;

    /**
     *
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;

    /**
     *
     */
    private float scoreProportion;

    /**
     *
     */
    private Integer scoreFull;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}