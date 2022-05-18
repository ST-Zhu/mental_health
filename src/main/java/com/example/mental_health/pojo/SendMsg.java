package com.example.mental_health.pojo;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @TableName tb_send_msg
 */
@TableName(value ="tb_send_msg")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendMsg implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    private String senderUsername;

    /**
     * 
     */
    private String receiverUsername;

    /**
     * 
     */
    private String message;

    /**
     * 
     */
    private String state;

    /**
     * 
     */
    private Date sendTime;

    /**
     * 
     */
    private Date readTime;

    /**
     * 
     */
    private Date dealTime;

    /**
     * 
     */
    @TableLogic
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}