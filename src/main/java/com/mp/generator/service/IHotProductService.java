package com.mp.generator.service;

import com.mp.generator.entity.HotAPIEntity;
import com.mp.generator.entity.HotProduct;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ayetony
 * @since 2020-09-29
 */
public interface IHotProductService extends IService<HotProduct> {


    public String getEntityByHotProductsJSON();

}
