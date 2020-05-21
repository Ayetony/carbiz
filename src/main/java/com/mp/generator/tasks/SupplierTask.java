package com.mp.generator.tasks;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mp.generator.entity.AlibabaSupplierInfoPo;
import com.mp.generator.entity.SupplierInfo;
import com.mp.generator.entity.SupplierInfoSync;
import com.mp.generator.mapper.AlibabaSupplierInfoPoMapper;
import com.mp.generator.mapper.SupplierInfoMapper;
import com.mp.generator.mapper.SupplierInfoSyncMapper;
import com.mp.generator.utils.HttpClientSupplierPuller;
import com.mp.generator.utils.UrlParse;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SupplierTask {

    @Autowired
    SupplierInfoSyncMapper supplierInfoSyncMapper;

    @Autowired
    SupplierInfoMapper supplierInfoMapper;

    @Autowired
    AlibabaSupplierInfoPoMapper alibabaSupplierInfoPoMap;


    //octopus sync supplier
    public void syncSupplierTableTest(){
        //删除dj
        int delQty = supplierInfoMapper.delete(new QueryWrapper<SupplierInfo>().like("shop_ref", "dj").or().eq(true,"shop_ref",""));
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
        hashMap.forEach((key,supplierInfo) -> {

            System.out.println("Scan Db===>扫描数据行数:" + scanPosition.incrementAndGet());
            SupplierInfoSync sync = new SupplierInfoSync();
            sync.setShopRef(key);
            sync.setBusinessType(supplierInfo.getBusinessType());
            sync.setCompanyName(supplierInfo.getCompanyName());
            sync.setKeyword(supplierInfo.getKeyword());
            sync.setMainProduct(supplierInfo.getMainProduct());
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
                System.out.println(sync.getSellerNick()  +""+  sync.getShopRef());
                supplierInfoSyncMapper.insert(sync);
                count.getAndIncrement();
                System.out.println("入库id:" + sync.getShopRef() + " count:" + count);
            }

        });
    }

    private void updateSupplierSync(SupplierInfoSync sync) {
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

    //todo
    public void aliBaseSupplier(){
        int delSellers = supplierInfoSyncMapper.delete(new QueryWrapper<SupplierInfoSync>().eq(true,"seller_nick","www"));
        System.out.println("删除空seller 数量:" +  delSellers);
        AtomicInteger count = new AtomicInteger();
        List<SupplierInfoSync> supplierInfoSyncList = supplierInfoSyncMapper.selectList(null);
        for (SupplierInfoSync sync : supplierInfoSyncList) {
            try {
                if (!isSupplierExist(sync.getShopRef())) {//不存在于生产数据表
                    count.incrementAndGet();
                    System.out.println("posting requests : " + count);
                    if (count.intValue() > 10000) {
                        System.exit(1);
                    }
                    importSupplierBase(sync);
                }else {
                    System.out.println("SKip existing supplier id :" + sync.getShopRef());
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("异常id" + sync.getShopRef());
            }
        }
    }

    @Async
    public Future<Long> importSupplierTask(List<SupplierInfoSync> supplierInfoSyncList, AtomicInteger count){
        for (SupplierInfoSync sync : supplierInfoSyncList) {
            try {
                if (!isSupplierExist(sync.getShopRef())) {//不存在于生产数据表
                    count.incrementAndGet();
                    System.out.println("posting requests : " + count);
                    if (count.intValue() > 100000) {
                        System.exit(1);
                    }
                    importSupplierBase(sync);
                }else {
                    System.out.println("SKip existing supplier id :" + sync.getShopRef());
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("异常id" + sync.getShopRef());
            }
        }
        return new AsyncResult<>(1L);

    }



    public List<SupplierInfoSync> segmentList(List<SupplierInfoSync> supplierInfoSyncList,int fromIndex, int toIndex){

        return supplierInfoSyncList.subList(fromIndex,toIndex);

    }

    private void importSupplierBase(SupplierInfoSync sync){
        AlibabaSupplierInfoPo alibabaSupplierInfoPo = HttpClientSupplierPuller.supplierPoFromJson(sync.getShopRef());
        if(alibabaSupplierInfoPo == null){
            System.out.println("Missing content");
            supplierInfoSyncMapper.deleteById(sync.getShopRef());
            System.out.println("delete the missing id" + sync.getShopRef());
        }else{
            alibabaSupplierInfoPo.setMainProduct(sync.getMainProduct());
            alibabaSupplierInfoPo.setShopName(sync.getCompanyName());
            alibabaSupplierInfoPo.setShopID(sync.getSellerNick());
            alibabaSupplierInfoPo.setSourceSite("1688.com");
            alibabaSupplierInfoPo.setBusinessType(sync.getBusinessType());
            if(alibabaSupplierInfoPo.getShopLocation().equals("null")){
                alibabaSupplierInfoPo.setShopLocation(sync.getShopLocation());
            }
            alibabaSupplierInfoPoMap.insert(alibabaSupplierInfoPo);
            System.out.println("供应商入库:" + alibabaSupplierInfoPo.getShopRef());
        }
    }

    //生产表 supplier po 存在对应的 shop 进行判断
    private boolean isSupplierExist(String shopRef){
        return alibabaSupplierInfoPoMap.selectCount(new QueryWrapper<AlibabaSupplierInfoPo>().eq("shop_ref",shopRef)) > 1 ;
    }





}
