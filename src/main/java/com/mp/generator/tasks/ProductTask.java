package com.mp.generator.tasks;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.google.common.collect.Multimap;
import com.mp.generator.entity.AlibabaProductInfoPo;
import com.mp.generator.entity.ProductInfoSync;
import com.mp.generator.mapper.AlibabaProductInfoPoMapper;
import com.mp.generator.mapper.ProductInfoSyncMapper;
import com.mp.generator.utils.HttpClientProductPuller;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ProductTask {

    @Autowired
    ProductInfoSyncMapper productInfoSyncMapper;

    @Autowired
    AlibabaProductInfoPoMapper alibabaProductInfoPoMapper;

    Random random = new Random();


    public List<ProductInfoSync> segmentList(List<ProductInfoSync> productInfoSyncList, int fromIndex, int toIndex){

        return productInfoSyncList.subList(fromIndex,toIndex);

    }

    private int tagSkip(ProductInfoSync sync) {
        // updateWrapper 更新指定的字段
        sync.setIsSkip(1);
        sync.setProductId("1165850125");
        LambdaUpdateWrapper<ProductInfoSync> updateWrapper = new UpdateWrapper<ProductInfoSync>().lambda();
        updateWrapper
                .set(ProductInfoSync::getIsSkip, 1)
                .eq(ProductInfoSync::getProductId, sync.getProductId());
        return productInfoSyncMapper.update(null, updateWrapper);
    }

    //判断是否跳过不存在sku的
    private boolean isSkuSkip(ProductInfoSync sync) {
        return sync.getIsSkip() == 1;
    }

    private boolean isExist(ProductInfoSync sync) {
        Integer count = alibabaProductInfoPoMapper.selectCount(new QueryWrapper<AlibabaProductInfoPo>().eq("product_i_d_in_source_site", sync.getProductId()));
        return count > 0;
    }

    private boolean importProductBase(ProductInfoSync sync) throws InterruptedException {
        Thread.sleep(random.nextInt(300)<<2);
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

    @Async
    public Future<Long> importProductTask(List<ProductInfoSync> productInfoSyncList, AtomicInteger count){
        for (ProductInfoSync sync : productInfoSyncList) {
            try {
                if (!isSkuSkip(sync) && !isExist(sync)) {//不存在于生产数据表，而且is_skip不为1

                    count.incrementAndGet();
                    System.out.println("posting requests : " + count);
                    if (count.intValue() > 50000) {
                        System.exit(1);
                    }

                    if (!importProductBase(sync)) {//api导入
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
        return  new AsyncResult<>(1L);
    }




}
