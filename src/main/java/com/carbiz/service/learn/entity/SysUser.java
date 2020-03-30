package com.carbiz.service.learn.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * ActiveRecord 模式调用
 * 每一个类的实例对象唯一对应一个数据表的一行
 */

@Data
@EqualsAndHashCode(callSuper = false)
@TableName(value = "sys_user")
public class SysUser extends Model<SysUser> {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String userName;
    private String userPassword;
    private String userEmail;
    private String UserInfo;
    private Byte[] headImg;
    private LocalDateTime createTime;
    @TableField(exist = false)
    private String remark;


}
