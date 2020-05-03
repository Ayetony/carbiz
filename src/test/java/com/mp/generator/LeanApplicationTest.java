package com.mp.generator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mp.generator.entity.AlibabaProductInfoPo;
import com.mp.generator.entity.ProductInfo;
import com.mp.generator.entity.ProductInfoSync;
import com.mp.generator.mapper.AlibabaProductInfoPoMapper;
import com.mp.generator.mapper.ProductInfoMapper;
import com.mp.generator.mapper.ProductInfoSyncMapper;
import com.mp.generator.utils.Extractor;
import com.mp.generator.utils.HttpClientProductPuller;
import com.mp.generator.utils.JsonType;
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
    public void testSelect() {//八爪鱼表数据删除dj链接，去重后导入到sync产品表
        //删除 product_info dj 临时链接
        int delDj = productInfoMapper.delete(new QueryWrapper<ProductInfo>().like("product_ref", "dj"));
        System.out.println("删除 product_info dj-link： " + delDj + "条");
        //同步表sync the table
        System.out.println(" base sync method test --------");
        List<ProductInfo> productInfos = productInfoMapper.selectList(new QueryWrapper<ProductInfo>().gt("id",600000));
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
            ProductInfoSync syncQuery = productInfoSyncMapper
                    .selectOne(new QueryWrapper<ProductInfoSync>().lambda().eq(ProductInfoSync::getProductId, productInfo.getProductId()));
            if (syncQuery != null) {
                String message = diff(syncQuery, sync);
                if (StringUtils.isNotBlank(message)) {
                    sync.setUpdateTimes(syncQuery.getUpdateTimes() + 1);
                    updateSync(sync);
                    System.out.println("update id=" + sync.getProductId() + ",更新:" + message + " ;updateCount" + updateCount.getAndIncrement());
                }else{
                    System.out.println("No need update ,equal id=" + sync.getProductId());
                }
            } else {
                productInfoSyncMapper.insert(sync);
                count.getAndIncrement();
                System.out.println("入库id:" + sync.getProductId() + " count:" + count);
            }
        });
        System.out.println("入库成功：" + count);
    }

    public void updateSync(ProductInfoSync sync) {
        LambdaUpdateWrapper<ProductInfoSync> updateWrapper = new UpdateWrapper<ProductInfoSync>().lambda();
        updateWrapper
                .set(ProductInfoSync::getUpdateTimes, sync.getUpdateTimes())
                .set(ProductInfoSync::getShopName, sync.getShopName())
                .set(ProductInfoSync::getCurrentPrice, sync.getCurrentPrice())
                .set(ProductInfoSync::getTotalSaleThisMonth, sync.getTotalSaleThisMonth())
                .set(ProductInfoSync::getKeyword,sync.getKeyword())
                .eq(ProductInfoSync::getProductId, sync.getProductId());
        productInfoSyncMapper.update(null, updateWrapper);
    }


    @Test
    public void testId() {
        ProductInfoSync sync = productInfoSyncMapper.selectById("44307401770");
        System.out.println(sync.getProductName());
    }

    public static String diff(ProductInfoSync old, ProductInfoSync sync) {

        String message = "";
        if (!StringUtils.equals(old.getProductName(), sync.getProductName())) {
            message += "Old-ProductName:" + old.getProductName() + "; New-ProductName:" + sync.getProductName();
        }

        if (!StringUtils.equals(old.getShopName(), sync.getShopName())) {
            message += "Old-ShopName:" + old.getShopName() + "; New-ShopName:" + sync.getShopName();
        }

        if (old.getCurrentPrice().intValue() != sync.getCurrentPrice().intValue()) {
            message += "Old-CurrentPrice:" + old.getCurrentPrice() + "; New-CurrentPrice:" + sync.getCurrentPrice();
        }

        if (old.getLoyalYears().intValue() != sync.getLoyalYears().intValue()) {
            message += "Old-LoyalYears:" + old.getLoyalYears() + "; New-LoyalYears:" + sync.getLoyalYears();
        }

        if (old.getTotalSaleThisMonth().intValue() != sync.getTotalSaleThisMonth().intValue()) {
            message += "Old-TotalSaleThisMonth:" + old.getTotalSaleThisMonth() + "; New-TotalSaleThisMonth:" + sync.getTotalSaleThisMonth();
        }

        if(!old.getKeyword().equals(sync.getKeyword())){
            message += "Old-keyword:" + old.getKeyword() + "; New-keyword:" + sync.getKeyword();
        }

        return message;


    }


    @Test
    public void testDj() {
        int qty = productInfoMapper.delete(new QueryWrapper<ProductInfo>().like("product_ref", "dj"));
        System.out.println(qty);
    }

    @Test
    public void testTrimColons() {
        HttpClientProductPuller.trimColons("1:4:尺码:XXXL 100公分");
    }

    //分类浙江
    @Test
    public void testClassify() {
        List<ProductInfoSync> syncList = productInfoSyncMapper.
                selectList(new QueryWrapper<ProductInfoSync>()
                        .lambda().like(ProductInfoSync::getKeyword, "浙江"));
        System.out.println(syncList.size());

        syncList.forEach(sync -> {
            String parent = null;
            String child = null;
            String keyword = sync.getKeyword();
            keyword = keyword.replace("浙江 ", "");
            String[] words = keyword.split(" ");
            if (words.length > 0) {
                parent = words[0];
                if (words.length > 1) {
                    child = words[1];
                }
            }
            if(StringUtils.isBlank(parent)){
                parent = child;
                sync.setParent(parent);
            }else{
                sync.setParent(parent);
                sync.setChild(child);
            }

            updateProductInfoSyncByKeyword(sync);
            System.out.println("分类入库：" + parent + "————————" + child);
        });

    }

    public void updateProductInfoSyncByKeyword(ProductInfoSync sync){
        LambdaUpdateWrapper<ProductInfoSync> updateWrapper = new UpdateWrapper<ProductInfoSync>().lambda();
        updateWrapper.set(ProductInfoSync::getParent,sync.getParent())
                .set(ProductInfoSync::getChild,sync.getChild())
                .eq(ProductInfoSync::getProductId,sync.getProductId());
        productInfoSyncMapper.update(null,updateWrapper);
    }

    //分类广东
    @Test
    public void testClassifyCanton() {
        List<ProductInfoSync> syncList = productInfoSyncMapper.
                selectList(new QueryWrapper<ProductInfoSync>()
                        .lambda().like(ProductInfoSync::getKeyword, "浙江"));
//                        .and(Wrapper -> Wrapper.isNull(ProductInfoSync::getChild)
//                                .or().eq(ProductInfoSync::getChild, "")));
        System.out.println(syncList.size());

        syncList.forEach(sync -> {
            String parent = null;
            String child = null;
            String keyword = sync.getKeyword();
            keyword = keyword.replace("浙江 ", "");
            String[] words = keyword.split(" ");
            if (words.length > 0) {
                parent = words[0];
                if (words.length > 1) {
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
    public void AliProductProduce() {
        AtomicInteger count = new AtomicInteger();
        List<ProductInfoSync> productInfoSyncList = productInfoSyncMapper.selectList(new QueryWrapper<ProductInfoSync>().like("keyword", "广东")
                .isNotNull(true, "parent").and(Wrapper -> Wrapper.isNotNull(true, "child")));
        for (ProductInfoSync sync : productInfoSyncList) {
            try {
                if (!isSkuSkip(sync) && !isExist(sync)) {//不存在于生产数据表，而且is_skip不为1

                    count.incrementAndGet();
                    System.out.println("posting requests : " + count);
                    if (count.intValue() > 10001) {
                        System.exit(1);
                    }

                    if (!normalBaseImport(sync)) {//api导入
                        int tag = tagSkip(sync);
                        System.out.println("request missing:" + sync.getProductId() + ";tagging skip qty:" + tag);
                        continue;
                    }
                }
                System.out.println("SKip existing sku product id :" + sync.getProductId());
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("异常id" + sync.getProductId());
            }
        }
    }

    //给产品同步表打标签test
    @Test
    public void tagSkipTest() {
        ProductInfoSync sync = new ProductInfoSync();
        sync.setIsSkip(1);
        sync.setProductId("1165850125");
        LambdaUpdateWrapper<ProductInfoSync> updateWrapper = new UpdateWrapper<ProductInfoSync>().lambda();
        updateWrapper
                .set(ProductInfoSync::getIsSkip, 1)
                .eq(ProductInfoSync::getProductId, sync.getProductId());
        int result = productInfoSyncMapper.update(null, updateWrapper);
        System.out.println(result);
    }


    public int tagSkip(ProductInfoSync sync) {
        // updateWrapper 更新指定的字段
        sync.setIsSkip(1);
        sync.setProductId("1165850125");
        LambdaUpdateWrapper<ProductInfoSync> updateWrapper = new UpdateWrapper<ProductInfoSync>().lambda();
        updateWrapper
                .set(ProductInfoSync::getIsSkip, 1)
                .eq(ProductInfoSync::getProductId, sync.getProductId());
        int result = productInfoSyncMapper.update(null, updateWrapper);
        return result;
    }


    //判断是否跳过不存在sku的
    public boolean isSkuSkip(ProductInfoSync sync) {
        return sync.getIsSkip() == 1;
    }


    public boolean isExist(ProductInfoSync sync) {
        Integer count = alibabaProductInfoPoMapper.selectCount(new QueryWrapper<AlibabaProductInfoPo>().eq("product_i_d_in_source_site", sync.getProductId()));
        return count > 0;
    }

    @Test
    public void testIsExist() {
        ProductInfoSync sync = new ProductInfoSync();
        sync.setProductId("1024077305");
        isExist(sync);

    }

    // 主键重复
    @Test
    public void testDuplicate() {
        ProductInfoSync sync = new ProductInfoSync();
        sync.setProductId("554203983307");
    }


    public boolean normalBaseImport(ProductInfoSync sync) {
        Map<AlibabaProductInfoPo, Multimap<String, String>> map = new HttpClientProductPuller().productInfoFromJson(sync.getProductId());//533816674053 614252193570
        if (map == null) {
            return false;
        }
        Map.Entry<AlibabaProductInfoPo, Multimap<String, String>> entry = map.entrySet().iterator().next();
        AtomicInteger count = new AtomicInteger();

        AlibabaProductInfoPo alibabaProductInfoPo = entry.getKey();
        Multimap<String, String> skus = entry.getValue();

        skus.entries().forEach(e -> {
            if (e.getValue() != null) {
                alibabaProductInfoPo.setSku(e.getValue());
            }
            alibabaProductInfoPo.setSizePriceStock(e.getKey());
            alibabaProductInfoPo.setSourceSite("1688.com");
            alibabaProductInfoPo.setParentCatalog(sync.getParent());
            alibabaProductInfoPo.setChildCatalog(sync.getChild());
            alibabaProductInfoPo.setKeyword(sync.getKeyword());
            alibabaProductInfoPo.setCrawlId(1);
            alibabaProductInfoPo.setCrawlLink("https://fuzhuang.1688.com/");
            alibabaProductInfoPoMapper.insert(alibabaProductInfoPo);
            count.incrementAndGet();
            System.out.println("正式入库：count" + count);
        });

        return true;
    }


    @Test
    public void testSplit() {
        String test = "颜色:粉底白点";
        System.out.println(test.split(";")[0]);
    }

    @Test
    public void testMultiMap(){
        HttpClientProductPuller puller = new HttpClientProductPuller();
        Map.Entry<AlibabaProductInfoPo, Multimap<String,String>> map =  puller.productInfoFromJson("581861251157").entrySet().iterator().next();

        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        AlibabaProductInfoPo alibabaProductInfoPo = map.getKey();
        JsonType type = new JsonType();
        type.setAlibabaProductInfoPo(alibabaProductInfoPo);
        type.setSkus(map.getValue().asMap());

        System.out.println(gson.toJson(type));
    }


}

