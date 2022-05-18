package com.example.mental_health.pojo;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @TableName tb_user_scale
 */
@TableName(value ="tb_user_scale")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserScale implements Serializable {
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
    private String userUsername;

    /**
     * 
     */
    private String scaleName;

    /**
     * 
     */
    private Integer grade;

    /**
     *
     */
    private String standard;


    /**
     * 
     */
    private String doctorUsername;

    /**
     *
     */
    private String doctorName;

    /**
     * 
     */
    private String doctorAdvice;

    /**
     *
     */
    private Date testTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}