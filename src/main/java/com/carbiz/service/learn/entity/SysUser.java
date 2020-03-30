package com.carbiz.service.learn.entity;

import java.util.Date;
import lombok.Data;

@Data
public class SysUser {

    private Integer id;
    private String userName;
    private String userPassword;
    private String userEmail;
    private String UserInfo;
    private Byte[] headImg;
    private Date createTime;


}
