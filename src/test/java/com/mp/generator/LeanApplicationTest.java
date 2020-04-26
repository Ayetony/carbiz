package com.mp.generator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mp.generator.entity.ProductInfo;
import com.mp.generator.entity.ProductInfoSync;
import com.mp.generator.mapper.ProductInfoMapper;
import com.mp.generator.mapper.ProductInfoSyncMapper;
import com.mp.generator.utils.Extractor;
import com.mp.generator.utils.UrlParse;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
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
class LearnApplicationTest {


    @Autowired
    private ProductInfoMapper productInfoMapper;

    @Autowired
    ProductInfoSyncMapper productInfoSyncMapper;


    @Test
    public void testSelect() {
        //删除 product_info dj 临时链接
        int delDj = productInfoMapper.delete(new QueryWrapper<ProductInfo>().like("product_ref","dj"));
        System.out.println("删除 product_info dj-link： " + delDj + "条");
        //同步表sync the table
        System.out.println(" base sync method test --------");
        List<ProductInfo> productInfos = productInfoMapper.selectList(null);
        AtomicInteger updateCount = new AtomicInteger();
        AtomicInteger scanPosition = new AtomicInteger();
        AtomicInteger count = new AtomicInteger();
        Map<String, ProductInfo> hashMap = new HashMap<>();// 一个hash打天下
        productInfos.forEach(user -> {
            user.setProductId(UrlParse.productRefToId(user.getProductRef()));
            user.setShopId(UrlParse.shopRefToId(user.getShopRef()));
            hashMap.put(user.getProductRef(), user);//不断载入，通过product_ref 就可以去重了
        });
//批量插入数据表
        hashMap.forEach((key, productInfo) -> {
            System.out.println("Scan Db===>扫描数据行数:" + scanPosition.incrementAndGet());
            ProductInfoSync sync = new ProductInfoSync();
            sync.setCurrentPrice(productInfo.getCurrentPrice());
            sync.setCustomerRebuyRate(productInfo.getCustomerRebuyRate());
            sync.setKeyword(productInfo.getKeyword());
            sync.setLoyalYears(Extractor.extractNumber(productInfo.getLoyalYears()));
            sync.setTotalSaleThisMonth(Extractor.extractNumber(productInfo.getTotalSaleThisMonth()));
            sync.setProductImgLink(productInfo.getProductImgLink());
            sync.setProductName(productInfo.getProductName());
            sync.setShopName(productInfo.getShopName());
            sync.setProductId(productInfo.getProductId());
            sync.setShopId(productInfo.getShopId());
            sync.setProductRef(productInfo.getProductRef());
            sync.setShopRef(productInfo.getShopRef());
            ProductInfoSync syncQuery = productInfoSyncMapper.selectById(productInfo.getProductId());
            if (syncQuery != null) {
                String message = diff(syncQuery,sync);
                if(StringUtils.isNotBlank(message)) {
                    sync.setUpdateTimes(syncQuery.getUpdateTimes() + 1);
                    productInfoSyncMapper.updateById(sync);
                    System.out.println("update id=" + sync.getProductId() + ",更新:" + message + " ;updateCount" + updateCount.getAndIncrement());
                }
            } else {
                productInfoSyncMapper.insert(sync);
                count.getAndIncrement();
                System.out.println("入库id:" + sync.getProductId() + " count:" + count);
            }
        });
        System.out.println("入库成功：" + count);

    }

    @Test
    public void testId(){
        ProductInfoSync sync = productInfoSyncMapper.selectById("44307401770");
        System.out.println(sync.getProductName());
    }

    public static String diff(ProductInfoSync old,ProductInfoSync sync){
//        if(!old.getProductName().equals(sync.getProductName()) || !old.getShopName().equals(sync.getShopName()) || old.getCurrentPrice()!=sync.getCurrentPrice() || old.getLoyalYears()!=sync.getLoyalYears() || old.getTotalSaleThisMonth() != sync.getTotalSaleThisMonth()){
//            return true;
//        }else{
//            return false;
//        }
        String message = "";
        if(!StringUtils.equals(old.getProductName(),sync.getProductName())){
           message += "Old-ProductName:" + old.getProductName() + "; New-ProductName:" + sync.getProductName();
        }

        if(!StringUtils.equals(old.getShopName(),sync.getShopName())){
            message += "Old-ShopName:" + old.getShopName() + "; New-ShopName:" + sync.getShopName();
        }

        if(old.getCurrentPrice().intValue()!=sync.getCurrentPrice().intValue()){
            message +=  "Old-CurrentPrice:" + old.getCurrentPrice() + "; New-CurrentPrice:" + sync.getCurrentPrice();
        }

        if(old.getLoyalYears().intValue()!=sync.getLoyalYears().intValue()){
            message += "Old-LoyalYears:" + old.getLoyalYears() + "; New-LoyalYears:" + sync.getLoyalYears();
        }

        if(old.getTotalSaleThisMonth().intValue() != sync.getTotalSaleThisMonth().intValue()){
            message += "Old-TotalSaleThisMonth:" + old.getTotalSaleThisMonth() + "; New-TotalSaleThisMonth:" + sync.getTotalSaleThisMonth();
        }

        return message;


    }


    @Test
    public void testDj(){
        int qty = productInfoMapper.delete(new QueryWrapper<ProductInfo>().like("product_ref","dj"));
        System.out.println(qty);
    }

}

