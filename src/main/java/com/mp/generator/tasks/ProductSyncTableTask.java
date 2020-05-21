package com.mp.generator.tasks;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.google.common.collect.Multimap;
import com.mp.generator.entity.AlibabaProductInfoPo;
import com.mp.generator.entity.ProductInfo;
import com.mp.generator.entity.ProductInfoSync;
import com.mp.generator.mapper.AlibabaProductInfoPoMapper;
import com.mp.generator.mapper.ProductInfoMapper;
import com.mp.generator.mapper.ProductInfoSyncMapper;
import com.mp.generator.utils.Extractor;
import com.mp.generator.utils.HttpClientProductPuller;
import com.mp.generator.utils.UrlParse;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@EnableScheduling
public class ProductSyncTableTask {

    @Autowired
    ProductInfoMapper productInfoMapper;

    @Autowired
    ProductInfoSyncMapper productInfoSyncMapper;

    @Autowired
    AlibabaProductInfoPoMapper alibabaProductInfoPoMapper;

    @Autowired
    ProductTask productTask;

    //sku 正式入库
//    @Scheduled(fixedRate = 1000*60*60*200)
    public void AliProductMultiTaskProduce() throws InterruptedException, ExecutionException {
        AtomicInteger count = new AtomicInteger();
        List<ProductInfoSync> productInfoSyncList = productInfoSyncMapper.selectList(new QueryWrapper<ProductInfoSync>().like("keyword", "广东")
                .isNotNull(true, "parent").and(Wrapper -> Wrapper.eq("is_skip",0)).orderByDesc("update_time"));
        int size = productInfoSyncList.size();
////        Future<Long> future01 = productTask.importProductTask(productTask.segmentList(productInfoSyncList,0,size/2),count);
        Future<Long> future02 = productTask.importProductTask(productTask.segmentList(productInfoSyncList,0,size/2-1),count);
        Future<Long> future03 = productTask.importProductTask(productTask.segmentList(productInfoSyncList,size/2,size*2/3-1),count);
        Future<Long> future04 = productTask.importProductTask(productTask.segmentList(productInfoSyncList,size*2/3,size),count);
        while (!future02.isDone() || !future03.isDone() || !future04.isDone()) {
            future02.get();
            future03.get();
            future04.get();
        }
    }


//    @Scheduled(cron = "0 0 4,12,22 * * ?")
    private void syncOcptusProductInfoTask() {
        //删除 product_info dj 临时链接
        int delDj = productInfoMapper.delete(new QueryWrapper<ProductInfo>().like("product_ref", "dj"));
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
            ProductInfoSync syncQuery = productInfoSyncMapper
                    .selectOne(new QueryWrapper<ProductInfoSync>().lambda().eq(ProductInfoSync::getProductId, productInfo.getProductId()));
            if (syncQuery != null) {
                String message = diff(syncQuery, sync);
                if (StringUtils.isNotBlank(message)) {
                    sync.setUpdateTimes(syncQuery.getUpdateTimes() + 1);
//                    productInfoSyncMapper.updateById(sync);
                    updateSync(sync);
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

//    @Scheduled(cron = "0 0 7,12,22 * * ?")
    private void classifyTask() {
        classifyZJprovince();
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

        return message;
    }


    public void classifyZJprovince() {
        List<ProductInfoSync> syncList = productInfoSyncMapper.
                selectList(new QueryWrapper<ProductInfoSync>()
                        .lambda().like(ProductInfoSync::getKeyword, "浙江")
                        .and(Wrapper -> Wrapper.isNull(ProductInfoSync::getChild)
                                .or().eq(ProductInfoSync::getChild, "")));


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
            productInfoSyncMapper.updateById(sync);
            System.out.println("分类：" + parent + "---" + child + "id:" + sync.getProductId());
        });

    }

    //正式入库
    public void AliProductProduce() {
        AtomicInteger count = new AtomicInteger();
        List<ProductInfoSync> productInfoSyncList = productInfoSyncMapper.selectList(new QueryWrapper<ProductInfoSync>().like("keyword", "浙江")
                .isNotNull(true, "parent").and(Wrapper -> Wrapper.isNotNull(true, "child")));

        for (ProductInfoSync sync : productInfoSyncList) {

            try {

                if (!isExist(sync)) {
                    AlibabaProductInfoPo alibabaProductInfoPo = new AlibabaProductInfoPo();
                    if (!normalBaseImport(sync)) {
                        System.out.println("rwquest missing:" + sync.getProductId());
                        continue;
                    }
                    count.incrementAndGet();
                    if (count.intValue() > 1000) {
                        System.exit(1);
                    }
                }
                System.out.println("SKip product id :" + sync.getProductId());


            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("异常id" + sync.getProductId());
            }
        }


    }

    //单条正式入库
    public boolean normalBaseImport(ProductInfoSync sync) {
        Map<AlibabaProductInfoPo, Multimap<String, String>> map = new HttpClientProductPuller().productInfoFromJson(sync.getProductId());//533816674053 614252193570
        if (map == null) {
            return false;
        }
        Map.Entry<AlibabaProductInfoPo, Multimap<String, String>> entry = map.entrySet().iterator().next();
        AtomicInteger count = new AtomicInteger();

        AlibabaProductInfoPo productInfoPo = entry.getKey();
        Multimap<String, String> skus = entry.getValue();

        skus.entries().forEach(e -> {
            if (e.getValue() != null) {
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

        return true;
    }

    public boolean isExist(ProductInfoSync sync) {

        Integer count = alibabaProductInfoPoMapper.selectCount(new QueryWrapper<AlibabaProductInfoPo>().eq("product_i_d_in_source_site", sync.getProductId()));
        return count > 0;

    }

    public void updateSync(ProductInfoSync sync) {
        LambdaUpdateWrapper<ProductInfoSync> updateWrapper = new UpdateWrapper<ProductInfoSync>().lambda();
        updateWrapper
                .set(ProductInfoSync::getUpdateTimes, sync.getUpdateTimes())
                .set(ProductInfoSync::getShopName, sync.getShopName())
                .set(ProductInfoSync::getCurrentPrice, sync.getCurrentPrice())
                .set(ProductInfoSync::getTotalSaleThisMonth, sync.getTotalSaleThisMonth())
                .eq(ProductInfoSync::getProductId, sync.getProductId());
        productInfoSyncMapper.update(null, updateWrapper);
    }


}
