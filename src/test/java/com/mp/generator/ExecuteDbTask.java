package com.mp.generator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mp.generator.entity.AlibabaSupplierInfoPo;
import com.mp.generator.entity.SupplierInfo;
import com.mp.generator.entity.SupplierInfoSync;
import com.mp.generator.mapper.AlibabaSupplierInfoPoMapper;
import com.mp.generator.mapper.SupplierInfoMapper;
import com.mp.generator.mapper.SupplierInfoSyncMapper;
import com.mp.generator.tasks.SupplierTask;
import com.mp.generator.utils.HttpClientSupplierPuller;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ExecuteDbTask {


    @Autowired
    SupplierInfoSyncMapper supplierInfoSyncMapper;

    @Autowired
    SupplierInfoMapper supplierInfoMapper;

    @Autowired
    AlibabaSupplierInfoPoMapper alibabaSupplierInfoPoMapper;

    @Autowired
    SupplierTask supplierTask;


    //octopus sync supplier
    @Test
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

    //  异步多任务
    @Test
    public void aliBaseSupplierTaskTest() throws ExecutionException, InterruptedException {
        int delSellers = supplierInfoSyncMapper.delete(new QueryWrapper<SupplierInfoSync>().eq(true,"seller_nick","www"));
        System.out.println("删除空seller 数量:" +  delSellers);
        AtomicInteger count = new AtomicInteger();
        List<SupplierInfoSync> supplierInfoSyncList = supplierInfoSyncMapper.selectList(null);
        int size = supplierInfoSyncList.size();
        Future<Long> future01 = supplierTask.importSupplierTask(supplierTask.segmentList(supplierInfoSyncList,0,size/2),count);
        while (!future01.isDone()) {
            future01.get();
        }
    }


    private boolean isSupplierExist(String shopRef){
        return alibabaSupplierInfoPoMapper.selectOne(new QueryWrapper<AlibabaSupplierInfoPo>().eq("shop_ref",shopRef)) != null ;
    }

    public void importBase(SupplierInfoSync sync){
        AlibabaSupplierInfoPo alibabaSupplierInfoPo = HttpClientSupplierPuller.supplierPoFromJson(sync.getShopRef());
        if(alibabaSupplierInfoPo == null){
            System.out.println("Missing content");
        }else{
            alibabaSupplierInfoPo.setMainProduct(sync.getMainProduct());
            alibabaSupplierInfoPo.setShopName(sync.getCompanyName());
            alibabaSupplierInfoPo.setShopID(sync.getSellerNick());
            alibabaSupplierInfoPo.setSourceSite("1688.com");
            alibabaSupplierInfoPo.setBusinessType(sync.getBusinessType());
            if(alibabaSupplierInfoPo.getShopLocation().equals("null")){
                alibabaSupplierInfoPo.setShopLocation(sync.getShopLocation());
            }
            alibabaSupplierInfoPoMapper.insert(alibabaSupplierInfoPo);
            System.out.println("供应商入库:" + alibabaSupplierInfoPo.getShopRef());
        }
    }



    @Test
    public void testIsSupplierExist(){
        System.out.println(isSupplierExist("https://0377888.1688.com/"));
    }


}
