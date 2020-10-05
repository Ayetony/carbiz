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
                isNotNull(HotProduct::getProductLink).isNotNull(HotProduct::getLogisticFee).isNotNull(HotProduct::getNumberOfComments).isNotNull(HotProduct::getMonthlyTurnover);
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
                String productPrice = hotProductMap.get(productLink).getProductPrice();
                String turnover = hotProduct.getMonthlyTurnover();
                String numOfcomments = hotProduct.getNumberOfComments();
                String crossBorderWeight = hotProduct.getCrossBorderWeight();


                turnover = turnover.replace("+","");
                if(StringUtils.contains(turnover,"万")) {
                    turnover = turnover.replace("万", "");
                    turnover = String.valueOf(Integer.parseInt(turnover) * 10000);
                }



                numOfcomments = numOfcomments.replace("+","");
                if(StringUtils.contains(numOfcomments,"万")) {
                    numOfcomments = numOfcomments.replace("万","");
                    numOfcomments = String.valueOf(Integer.parseInt(numOfcomments) * 10000);
                }

                hotProduct.setNumberOfComments(numOfcomments);
                hotProduct.setMonthlyTurnover(turnover);
                hotProduct.setCrossBorderWeight(StringUtils.strip(crossBorderWeight).replace("kg",""));


                try {
                    List<String> doubleList = Extractor.trimToString(productPrice);
                    if(doubleList.size() > 0) {
                        entity.setMinPrice(Extractor.getMin(doubleList));
                        entity.setMaxPrice(Extractor.getMax(doubleList));
                        entity.setHotProduct(hotProduct);
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
