package com.example.mental_health.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @TableName tb_user
 */
@TableName(value ="tb_user")
@Data
public class User implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * import org.hibernate.validator.constraints.Length;
     */
    @NotNull(message = "信息不能为空")
    @Length(min = 1,max = 15,message = "请输入长度为1到15位的用户名")
    private String username;

    /**
     *
     */
    @NotNull(message = "信息不能为空")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "密码由数字和大小写字母组成，长度为6到18位")
    private String password;

    /**
     *
     */
    @NotNull(message = "信息不能为空")
    private String role;

    /**
     *
     */
    @Email(message = "请输入正确格式的邮箱")
    private String email;

    /**
     *
     */
    @NotNull(message = "信息不能为空")
    @Length(min = 1,max = 15,message = "请输入1到15的昵称长度")
    private String nickname;

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
    @TableLogic
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}