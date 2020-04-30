package com.mp.generator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mp.generator.entity.SupplierInfo;
import com.mp.generator.mapper.SupplierInfoMapper;
import com.mp.generator.mapper.SupplierInfoSyncMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ExecuteDbTask {


    @Autowired
    SupplierInfoSyncMapper supplierInfoSyncMapper;

    @Autowired
    SupplierInfoMapper supplierInfoMapper;


    //octopus sync supplier
    @Test
    public void syncSupplierTableTest(){

        //删除dj
        int delQty = supplierInfoMapper.delete(new QueryWrapper<SupplierInfo>().like("product_ref", "dj"));
        System.out.println("删除垃圾DJ链接数量 : " + delQty);
        //查询所有并去重
        System.out.println(" base sync method test --------");
        List<SupplierInfo> supplierInfos = supplierInfoMapper.selectList(null);
        AtomicInteger updateCount = new AtomicInteger();
        AtomicInteger scanPosition = new AtomicInteger();
        AtomicInteger count = new AtomicInteger();
        Map<String, SupplierInfo> hashMap = new HashMap<>();// 一个hash打天下
        supplierInfos.forEach(supplierInfo -> {
            hashMap.put(supplierInfo.getShopRef(), supplierInfo);//不断载入，通过product_ref 就可以去重了
        });

        //todo  shop_ref 去重    shop_ref 转化为 shop_nick





    }


}
