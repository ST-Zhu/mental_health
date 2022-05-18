package com.example.mental_health.pojo;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 
 * @TableName tb_question
 */
@TableName(value ="tb_question")
@Data
public class Question implements Serializable {
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
    private String theme;

    /**
     * 
     */
    private String optionA;

    /**
     * 
     */
    private String optionB;

    /**
     * 
     */
    private String optionC;

    /**
     * 
     */
    private String optionD;

    /**
     * 
     */
    private Integer valueA;

    /**
     * 
     */
    private Integer valueB;

    /**
     * 
     */
    private Integer valueC;

    /**
     * 
     */
    private Integer valueD;

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
    private String scaleName;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}