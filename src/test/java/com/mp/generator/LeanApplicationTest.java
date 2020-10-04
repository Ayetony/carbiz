package com.mp.generator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mp.generator.entity.*;
import com.mp.generator.mapper.*;
import com.mp.generator.service.impl.HotProductServiceImpl;
import com.mp.generator.tasks.ProductTask;
import com.mp.generator.utils.*;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
@RunWith(SpringRunner.class)
class LearnApplicationTest {


    @Autowired
    ProductInfoSyncMapper productInfoSyncMapper;
    @Autowired
    AlibabaProductInfoPoMapper alibabaProductInfoPoMapper;
    @Autowired
    ProductTask productTask;
    @Autowired
    private ProductInfoMapper productInfoMapper;
    @Autowired
    private HotProductMapper hotProductMapper;

    @Autowired
    private HotCrossborderProductMapper hotCrossborderProductMapper;

    @Autowired
    private HotProductServiceImpl hotProductService;

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
    public void testSelect() {//八爪鱼表数据删除dj链接，去重后导入到sync产品表
        //删除 product_info dj 临时链接
        int delDj = productInfoMapper.delete(new QueryWrapper<ProductInfo>().like("product_ref", "dj"));
        System.out.println("删除 product_info dj-link： " + delDj + "条");
        //同步表sync the table
        System.out.println(" base sync method test --------");
        List<ProductInfo> productInfos = productInfoMapper.selectList(new QueryWrapper<ProductInfo>().gt("id",0));// 一次数量不应超过50万条
        AtomicInteger updateCount = new AtomicInteger();
        AtomicInteger scanPosition = new AtomicInteger();
        AtomicInteger count = new AtomicInteger();
        Map<String, ProductInfo> hashMap = new HashMap<>();// 一个hash打天下
        productInfos.forEach(user -> {
            user.setProductId(UrlParse.productRefToId(user.getProductRef()));
            user.setShopId(UrlParse.shopRefToId(user.getShopRef()));
            hashMap.put(user.getProductRef(), user);//不断载入，通过product_ref 就可以去重了
        });
//批量插入数据表 todo
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

    //分类母婴和家电
    @Test
    public void testBabyAndApplianceClassify() {
        List<ProductInfoSync> syncList = productInfoSyncMapper.
                selectList(new QueryWrapper<ProductInfoSync>()
                        .lambda().like(ProductInfoSync::getKeyword, "母婴").or().like(ProductInfoSync::getKeyword, "家电"));
        syncList.forEach(sync -> {
            String parent = null;
            String child = null;
            String keyword = sync.getKeyword();
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


    //避免空值传递
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
                        .lambda().like(ProductInfoSync::getKeyword, "广东"));
//                        .and(Wrapper -> Wrapper.isNull(ProductInfoSync::getChild)
//                                .or().eq(ProductInfoSync::getChild, "")));
        System.out.println(syncList.size());

        syncList.forEach(sync -> {
            String parent = null;
            String child = null;
            String keyword = sync.getKeyword();
            keyword = keyword.replace("广东 ", "");
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
            System.out.println("分类：" + parent + "---" + child);
        });

    }

    @Test
    public void AliProductProduce() throws InterruptedException, ExecutionException {
        AtomicInteger count = new AtomicInteger();
        List<ProductInfoSync> productInfoSyncList = productInfoSyncMapper.selectList(new QueryWrapper<ProductInfoSync>().like("keyword", "母婴")
                .isNotNull(true, "parent").and(Wrapper -> Wrapper.eq("is_skip",0)).orderByDesc());
        int size = productInfoSyncList.size();
        Future<Long> future01 = productTask.importProductTask(productTask.segmentList(productInfoSyncList,size*3/4,size),count);
        while (!future01.isDone()) {
            future01.get();
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
            if(StringUtils.indexOf(sync.getKeyword(),"母婴")!=-1){
                alibabaProductInfoPo.setCrawlId(2);
                alibabaProductInfoPo.setCrawlLink("https://muying.1688.com/");
            }else if(StringUtils.indexOf(sync.getKeyword(),"家电")!=-1){
                alibabaProductInfoPo.setCrawlId(3);
                alibabaProductInfoPo.setCrawlLink("https://jiadian.1688.com/");
            }else {
                alibabaProductInfoPo.setCrawlId(1);
                alibabaProductInfoPo.setCrawlLink("https://fuzhuang.1688.com/");
            }
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

    @Test
    public void generateExcel() throws IOException {
        List<AlibabaProductInfoPo>  productInfoPoList =  alibabaProductInfoPoMapper.
                selectList(new QueryWrapper<AlibabaProductInfoPo>()
                        .gt("id",200000).and(Wrapper -> Wrapper.lt("id",500000)));
        ExcelProcess.itemsSkuToExcel(productInfoPoList);
    }

    @Test
    public void getHotProducts(){

        List<HotProduct> list = hotProductMapper.selectList(null);
        System.out.println(list.size());

    }

    @Test
    public void getActualHotPro(){

        QueryWrapper<HotProduct> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().isNotNull(HotProduct::getProcurementRepetitionRate).isNotNull(HotProduct::getOriginalDeliverAddr).
                isNotNull(HotProduct::getProductLink).isNotNull(HotProduct::getLogisticFee);
        List<HotProduct> hotProducts = hotProductMapper.selectList(queryWrapper);
        List<String> links = new ArrayList<>() ;
        Map<String,HotProduct> hotProductMap = new HashMap<>();

        hotProducts.forEach( hotProduct -> {
            String link = StringUtils.trim(hotProduct.getProductLink());
            links.add(link);
            hotProductMap.put(link,hotProduct);
        });

        QueryWrapper<HotCrossborderProduct> hotCrossborderProductQueryWrapper = new QueryWrapper<>();
        hotCrossborderProductQueryWrapper.lambda().in(HotCrossborderProduct::getProductLink,links);
        List<HotCrossborderProduct> hotCbList = hotCrossborderProductMapper.selectList(hotCrossborderProductQueryWrapper);


        List<HotAPIEntity> hotAPIEntityList = new ArrayList<>();

        for (HotCrossborderProduct cb : hotCbList) {
            HotAPIEntity entity = new HotAPIEntity();
            String productLink = cb.getProductLink();

            if(!hotAPIEntityList.contains(cb) && hotProductMap.containsKey(productLink)){
                entity.setHotCrossborderProduct(cb);
                HotProduct hotProduct = hotProductMap.get(productLink);
                entity.setHotProduct(hotProduct);
                String productPrice = hotProductMap.get(productLink).getProductPrice();
                String turnover = hotProduct.getMonthlyTurnover();
                if(turnover.indexOf("万") != -1){
                    turnover.replace("万", "");
                }
                try {
                    List<String> doubleList = Extractor.trimToString(productPrice);
                    if(doubleList.size() > 0) {
                        entity.setMinPrice(doubleList.get(0));
                        entity.setMaxPrice(doubleList.get(doubleList.size() - 1));
                        entity.getHotProduct().setMonthlyTurnover(turnover);
                        entity.getHotProduct().setProductPrice(productPrice);
                        hotAPIEntityList.add(entity);
                    }
                }catch (NumberFormatException e){
                    continue;
                }
            }
        }
        Gson gson = new GsonBuilder().create();
//        System.out.println(gson.toJson(hotAPIEntityList));
    }


    @Test
    public void getHotCbProducts(){
        System.out.println(hotCrossborderProductMapper.selectList(null).size());
    }

    @Test
    public void testHotProFormatter(){
        System.out.println(hotProductService.getEntityByHotProductsJSON());
    }

}

