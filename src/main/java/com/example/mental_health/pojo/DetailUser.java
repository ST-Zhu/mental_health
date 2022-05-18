package com.example.mental_health.pojo;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * 
 * @TableName tb_detail_msg
 */
@TableName(value ="tb_detail_user")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetailUser implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    @NotNull(message = "信息不能为空")
    @Length(min = 1,max = 15,message = "请输入长度为1到15位的用户名")
    private String username;

    /**
     *
     */
    @NotNull(message = "信息不能为空")
    private String role;

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
    @Positive(message = "年龄不是正整数")
    @Max(value = 125,message = "年龄不能大于125")
    @Min(value = 1,message = "年龄不能小于1")
    private Integer age;

    /**
     * 
     */
    private String sex;

    /**
     * 
     */
    @Positive(message = "请输入正确格式的电话号码")
    @Length(min = 11,max = 11,message = "请输入11位的电话号码")
    private String phone;

    /**
     * 
     */
    private Double finer;

    /**
     * 
     */
    private Long appointNum;

    /**
     * 
     */
    private Long completeNum;

    /**
     * 
     */
    @Length(min = 50,max = 500,message = "个人简介为50到500字")
    private String resume;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}