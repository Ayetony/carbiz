package com.mp.generator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mp.generator.entity.SupplierInfo;
import com.mp.generator.entity.SupplierInfoSync;
import com.mp.generator.mapper.SupplierInfoMapper;
import com.mp.generator.mapper.SupplierInfoSyncMapper;
import com.mp.generator.utils.UrlParse;
import org.apache.commons.lang.StringUtils;
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
        int delQty = supplierInfoMapper.delete(new QueryWrapper<SupplierInfo>().like("shop_ref", "dj").or().isNull("shop_ref"));
        System.out.println("删除垃圾DJ链接数量 : " + delQty);
        //查询所有并去重
        System.out.println(" mysql base sync supplier method testing --------");
        List<SupplierInfo> supplierInfos = supplierInfoMapper.selectList(null);
        AtomicInteger updateCount = new AtomicInteger();
        AtomicInteger scanPosition = new AtomicInteger();
        AtomicInteger count = new AtomicInteger();
        Map<String, SupplierInfo> hashMap = new HashMap<>();// 一个hash打天下
        supplierInfos.forEach(supplierInfo -> {
            hashMap.put(supplierInfo.getShopRef(), supplierInfo);//不断载入，通过product_ref 就可以去重了
        });
        List<SupplierInfo> supplierInfoList = supplierInfoMapper.selectList(null);
        supplierInfoList.forEach( supplierInfo -> {
            hashMap.put(supplierInfo.getShopRef(),supplierInfo);
        });

        hashMap.forEach((key,supplierInfo) -> {

            System.out.println("Scan Db===>扫描数据行数:" + scanPosition.incrementAndGet());

            SupplierInfoSync sync = new SupplierInfoSync();
            sync.setShopRef(key);
            sync.setBusinessType(supplierInfo.getBusinessType());
            sync.setCompanyName(supplierInfo.getCompanyName());
            sync.setKeyword(supplierInfo.getKeyword());
            sync.setMainProduct(supplierInfo.getMainProduct());
//            System.out.println(UrlParse.shopRefToSellerNick(supplierInfo.getShopRef()) + supplierInfo.getShopRef());
            sync.setSellerNick(UrlParse.shopRefToSellerNick(supplierInfo.getShopRef()));
            sync.setShopLocation(supplierInfo.getShopLocation());
            sync.setTotalSold(supplierInfo.getTotalSold());

            SupplierInfoSync syncQuery = supplierInfoSyncMapper
                    .selectOne(new QueryWrapper<SupplierInfoSync>().lambda().eq(SupplierInfoSync::getShopRef, key));
            if (syncQuery != null) {
                String message = diff(syncQuery, sync);
                if (StringUtils.isNotBlank(message)) {
                    updateSupplierSync(sync);
                    System.out.println("update id=" + sync.getShopRef() + ",更新:" + message + " ;updateCount" + updateCount.getAndIncrement());
                }else{
                    System.out.println("No need update ,equal id=" + sync.getShopRef());
                }
            } else {
                supplierInfoSyncMapper.insert(sync);
                count.getAndIncrement();
                System.out.println("入库id:" + sync.getShopRef() + " count:" + count);
            }

        });
    }

    public void updateSupplierSync(SupplierInfoSync sync) {
        LambdaUpdateWrapper<SupplierInfoSync> updateWrapper = new UpdateWrapper<SupplierInfoSync>().lambda();
        updateWrapper.set(SupplierInfoSync::getCompanyName, sync.getCompanyName())
                .set(SupplierInfoSync::getKeyword,sync.getKeyword())
                .set(SupplierInfoSync::getMainProduct,sync.getMainProduct())
                .set(SupplierInfoSync::getBusinessType,sync.getBusinessType())
                .set(SupplierInfoSync::getTotalSold,sync.getTotalSold())
                .eq(SupplierInfoSync::getShopRef, sync.getShopRef());
        supplierInfoSyncMapper.update(null, updateWrapper);
    }

    private  String diff(SupplierInfoSync old, SupplierInfoSync sync) {
        String message = "";
        if (!StringUtils.equals(old.getCompanyName(), sync.getCompanyName())) {
            message += "Old-CompanyName:" + old.getCompanyName() + "; New-CompanyName:" + sync.getCompanyName();
        }

        if (!StringUtils.equals(old.getBusinessType(), sync.getBusinessType())) {
            message += "Old-BusinessType:" + old.getBusinessType() + "; New-BusinessType:" + sync.getBusinessType();
        }

        if (!StringUtils.equals(old.getMainProduct(), sync.getMainProduct())) {
            message += "Old-MainProduct:" + old.getMainProduct() + "; New-MainProduct:" + sync.getMainProduct();
        }

        if(!StringUtils.equals(old.getKeyword(),sync.getKeyword())){
            message += "Old-keyword:" + old.getKeyword() + "; New-keyword:" + sync.getKeyword();
        }

        return message;
    }



}
