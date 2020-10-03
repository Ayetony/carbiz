package com.mp.generator.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mp.generator.entity.HotAPIEntity;
import com.mp.generator.entity.HotCrossborderProduct;
import com.mp.generator.entity.HotProduct;
import com.mp.generator.mapper.HotCrossborderProductMapper;
import com.mp.generator.mapper.HotProductMapper;
import com.mp.generator.service.IHotProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mp.generator.utils.Extractor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ayetony
 * @since 2020-09-29
 */
@Service
public class HotProductServiceImpl extends ServiceImpl<HotProductMapper, HotProduct> implements IHotProductService {

    @Autowired
    private HotProductMapper hotProductMapper;

    @Autowired
    private  HotCrossborderProductMapper hotCrossborderProductMapper;

    @Override
    public String getEntityByHotProductsJSON() {
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

                System.out.println(productPrice);

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
        return gson.toJson(hotAPIEntityList);
    }


}
