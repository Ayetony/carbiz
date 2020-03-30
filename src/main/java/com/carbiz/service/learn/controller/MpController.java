package com.carbiz.service.learn.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mp.generator.entity.SysUser;
import com.mp.generator.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MpController {

    @RequestMapping("/mp")
    public String map(){
        return "你好";
    }

    @Autowired
    ISysUserService iSysUserService;

    @RequestMapping("/hello")
    public List<SysUser> hello(){
        List<SysUser> list = iSysUserService.getBaseMapper().selectList(new QueryWrapper<SysUser>().lambda().like(SysUser::getUserInfo,"test"));
        return list;
    }

}
