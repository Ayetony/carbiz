package com.carbiz.service.learn;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.carbiz.service.learn.entity.SysUser;
import com.carbiz.service.learn.mapper.SysUserMapper;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@SpringBootTest
class LearnApplicationTests {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Test
    public void testSelect(){
        System.out.println(" select all method test --------");
        List<SysUser> users = sysUserMapper.selectList(null);
        users.forEach(System.out::println);
    }

    @Test
    public void testInsert(){
        SysUser user = new SysUser();
        user.setCreateTime(LocalDateTime.now());
        user.setUserEmail("ayetony.miao@gmail.com");
        user.setUserInfo("how you survive in an outbreak of pandemic");
        user.setUserName("John");
        user.setUserPassword("pwd123");
        boolean result = user.insert();
        Assert.assertEquals(true,result);
    }

    @Test
    public void updateByWrapper(){
        LambdaUpdateWrapper<SysUser> updateWrapper = new UpdateWrapper<SysUser>().lambda();
        updateWrapper.eq(SysUser::getId,1008);
        SysUser user = new SysUser();
        user.setCreateTime(LocalDateTime.now());
        user.setUserPassword("hello123");
        int result = sysUserMapper.update(user, updateWrapper);
        Assert.assertEquals(result, 1);
    }

    @Test
    public void updateByWrapperLambda(){
        LambdaUpdateWrapper<SysUser> updateWrapper = new UpdateWrapper<SysUser>().lambda();
        updateWrapper.eq(SysUser::getId,1008)
                .set(SysUser::getUserEmail,"pps@163.com");
        int result = sysUserMapper.update(null,updateWrapper);
        Assert.assertEquals(result,1);
    }

    @Test
    public void updateByWrapperChain(){
        LambdaUpdateChainWrapper<SysUser> updateChainWrapper = new LambdaUpdateChainWrapper<SysUser>(sysUserMapper);
        boolean rs = updateChainWrapper.set(SysUser::getCreateTime,LocalDateTime.now())
                .set(SysUser::getUserInfo, "hello the new world")
                .eq(SysUser::getId,1007)
                .update();
        Assert.assertEquals(rs, true);
    }

    @Test
    public void deleteById(){
        Integer id = 1006;
        int rs = sysUserMapper.deleteById(id);
        Assert.assertEquals(rs,1);
    }

    @Test
    public void deleteByWrapper(){
        String params = "1001,1005,1007";
        List<String> paramsList = Arrays.asList(params.split(","));
        LambdaQueryWrapper<SysUser> queryWrapper = new QueryWrapper<SysUser>().lambda();
//        queryWrapper.like(SysUser::getUserName,"test").in(SysUser::getId, paramsList);
//        sysUserMapper.selectList(queryWrapper).forEach(System.out::println);
        queryWrapper.like(SysUser::getUserName,"test").in(SysUser::getId,paramsList);
        int rs = sysUserMapper.delete(queryWrapper);
        System.out.println(rs);
    }

    @Test
    public void getByWrapper3(){
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("user_email", "miao")
                .or()
                .orderByDesc("create_time").orderByAsc("id");
        sysUserMapper.selectList(queryWrapper).forEach(System.out::println);
    }


    /*
    sub query ,子查询，Wrappers 设置查询条件
     */
    @Test
    public void getByWrapper4(){
        QueryWrapper<SysUser> queryWrapper = Wrappers.query();
        //date_format(create_time,'%Y-%m-%d')={0}
        queryWrapper.apply("user_email", "ayetony.miao@gmail.com")
                .inSql("id","select id from sys_user");
        List<SysUser> list = sysUserMapper.selectList(queryWrapper);
        list.forEach(System.out::println);
    }

}
