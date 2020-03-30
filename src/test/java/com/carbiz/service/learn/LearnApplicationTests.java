package com.carbiz.service.learn;

import com.carbiz.service.learn.entity.SysUser;
import com.carbiz.service.learn.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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

}
