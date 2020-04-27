package com.mp.generator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.google.common.collect.Multimap;
import com.mp.generator.entity.AlibabaProductInfoPo;
import com.mp.generator.entity.ProductInfo;
import com.mp.generator.entity.ProductInfoSync;
import com.mp.generator.mapper.AlibabaProductInfoPoMapper;
import com.mp.generator.mapper.ProductInfoMapper;
import com.mp.generator.mapper.ProductInfoSyncMapper;
import com.mp.generator.utils.Extractor;
import com.mp.generator.utils.HttpClientPuller;
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

    @Autowired
    AlibabaProductInfoPoMapper alibabaProductInfoPoMapper;



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

    public static String diff(ProductInfoSync old, ProductInfoSync sync){

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

    @Test
    public  void testTrimColons(){
        HttpClientPuller.trimColons("1:4:尺码:XXXL 100公分");
    }

    //分类
    @Test
    public void testClassify(){
        List<ProductInfoSync> syncList = productInfoSyncMapper.
                selectList(new QueryWrapper<ProductInfoSync>().lambda().like(ProductInfoSync::getKeyword,"浙江")
                .isNull(true,ProductInfoSync::getParent).or().eq(ProductInfoSync::getChild,""));;
        System.out.println(syncList.size());

        syncList.forEach( sync ->{
            String parent = null;
            String child = null;
            String keyword = sync.getKeyword();
            keyword = keyword.replace("浙江 ","");
            String[] words = keyword.split(" ");
            if(words.length > 0){
                parent = words[0];
                if(words.length>1) {
                    child = words[1];
                }
            }
            sync.setParent(parent);
            sync.setChild(child);
            productInfoSyncMapper.updateById(sync);
            System.out.println("分类：" + parent + "---" + child);
        });

    }

    @Test
    public void AliProductProduce(){
        AtomicInteger count = new AtomicInteger();
        List<ProductInfoSync> productInfoSyncList = productInfoSyncMapper.selectList(new QueryWrapper<ProductInfoSync>()
                .isNotNull(true,"parent").isNotNull(true,"child").or()
                .ne(true,"child","").ne(true,"parent",""));
        productInfoSyncList.forEach(sync ->{
            AlibabaProductInfoPo alibabaProductInfoPo = new AlibabaProductInfoPo();
            try{
                testNormalBaseImport(sync);
            }catch (IllegalStateException e){
                throw new RuntimeException("Not a Json object 异常 id:" + sync.getProductId());
            }
            count.incrementAndGet();
            if(count.intValue() > 100){
                System.exit(1);
            }
        });

    }

    public void testNormalBaseImport(ProductInfoSync sync){
        Map<AlibabaProductInfoPo, Multimap<String,String>> map = new HttpClientPuller().productInfoFromJson(sync.getProductId());//533816674053 614252193570
        Map.Entry<AlibabaProductInfoPo, Multimap<String, String>> entry =  map.entrySet().iterator().next();
        AtomicInteger count = new AtomicInteger();

        AlibabaProductInfoPo productInfoPo = entry.getKey();
        Multimap<String,String> skus = entry.getValue();

        skus.entries().forEach( e -> {
            if(e.getValue() != null) {
                productInfoPo.setSku(e.getValue());
            }
            productInfoPo.setSizePriceStock(e.getKey());
            productInfoPo.setSourceSite("1688.com");
            productInfoPo.setParentCatalog(sync.getParent());
            productInfoPo.setChildCatalog(sync.getChild());
            productInfoPo.setKeyword(sync.getKeyword());
            alibabaProductInfoPoMapper.insert(productInfoPo);
            count.incrementAndGet();
            System.out.println("正式入库：count" + count);
        });
    }


   @Test
    public void testSplit(){
        String test = "颜色:粉底白点";
       System.out.println(test.split(";")[0]);
   }


}

