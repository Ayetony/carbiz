package com.mp.generator.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mp.generator.entity.SysUser;
import com.mp.generator.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author ayetony
 * @since 2020-03-31
 */
@RestController
@RequestMapping("/generator/sysUser")
public class SysUserController {

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
