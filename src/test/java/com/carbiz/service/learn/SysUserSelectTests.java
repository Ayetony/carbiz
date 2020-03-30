package com.carbiz.service.learn;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carbiz.service.learn.entity.SysUser;
import com.carbiz.service.learn.mapper.SysUserMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SysUserSelectTests {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Test
    public  void getUserList(){
        List<SysUser> list = sysUserMapper.selectList(null);
        list.forEach(System.out::println);
    }

    @Test
    public void getByMap(){
        Map<String,Object> columnMap = new HashMap<>();
        columnMap.put("id", 1001);
        columnMap.put("id", 1007);
        columnMap.put("id", 1);
        List<SysUser> list = sysUserMapper.selectByMap(columnMap);
        list.forEach(System.out::println);
    }
     /*
      复杂查询，字段来自数据库，区分pojo
     * */
    @Test
    public void getByWrapper(){
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("user_name","john").between("id",1,1009).isNull("headmg");
        sysUserMapper.selectList(queryWrapper).forEach(System.out::println);
    }

    @Test
    public void getByWrapper6(){
        QueryWrapper<SysUser> queryWrapper = Wrappers.query();
        queryWrapper.like("user_name","who")
                .or(
                        sql->sql.le("id",1006).isNull("head_img")
                ).ge("id",1);
        sysUserMapper.selectList(queryWrapper).forEach(System.out::println);
    }

    @Test
    public void getWrapper7(){
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        String positionKey = "hello";
        String headImage = null;
        queryWrapper.like(StringUtils.isNotBlank(positionKey),"user_info",positionKey)
                .ge(StringUtils.isNotBlank(headImage),"head_img",headImage)
                .orderByDesc("create_time")
                .select("id","user_info","create_time");
        sysUserMapper.selectList(queryWrapper).forEach(System.out::println);
    }


    @Test
    public void getByEntity(){
        SysUser user = new SysUser();
        user.setUserName("who am i");
        QueryWrapper<SysUser> queryWrapper = Wrappers.query(user);
        sysUserMapper.selectList(queryWrapper).forEach(System.out::println);
    }

    //分页 page
    @Test
    public void getPage(){
        LambdaQueryWrapper<SysUser> lambdaQueryWrapper = new QueryWrapper<SysUser>().lambda();
        lambdaQueryWrapper.like(SysUser::getUserName,"who")
                .and(sql->sql.ge(SysUser::getId,1).or().isNull(SysUser::getHeadImg));
        long current = 2;
        long size =2;
        Page<SysUser> page = new Page<SysUser>(current,size);
        IPage<SysUser> iPage = sysUserMapper.selectPage(page,lambdaQueryWrapper);
        iPage.getRecords().forEach(System.out::println);
        iPage.setTotal(iPage.getRecords().size());
        System.out.println(iPage.getPages()+"--current:"+iPage.getCurrent() + "--total records:"+iPage.getTotal());
        List<SysUser> list;
        if(current*size<=iPage.getTotal()){
            //动态数组list 截取，提供给指定的页面record
            list = new ArrayList<>(iPage.getRecords().subList((int) ((current-1)*size) - 1, (int) (current*size) - 1));
        }else{
            list = new ArrayList<>(iPage.getRecords().subList((int) ((current-1)*size) - 1,iPage.getRecords().size() - 1));
        }
        list.forEach(System.out::println);
    }

    @Test
    public void getMorePage(){
        String userInfo = "test";
        LambdaQueryWrapper<SysUser> lambdaQueryWrapper = new QueryWrapper<SysUser>().lambda();
        lambdaQueryWrapper.like(StringUtils.isNotBlank(userInfo),SysUser::getUserInfo,userInfo);
        int current = 1;
        int size =2;
        // 返回 key value
        Page<Map<String,Object>> page = new Page<>(current, size);
        IPage<Map<String,Object>> iPage = sysUserMapper.selectMapsPage(page, lambdaQueryWrapper);
        iPage.getRecords().forEach(map-> map.forEach((key,value)-> System.out.println(key + ":" + value)));
    }


}

