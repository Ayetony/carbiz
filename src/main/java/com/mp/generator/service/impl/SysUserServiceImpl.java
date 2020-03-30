package com.mp.generator.service.impl;

import com.mp.generator.entity.SysUser;
import com.mp.generator.mapper.SysUserMapper;
import com.mp.generator.service.ISysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author ayetony
 * @since 2020-03-31
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

}
