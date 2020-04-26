package com.mp.generator.service.impl;

import com.mp.generator.entity.ProductInfo;
import com.mp.generator.mapper.ProductInfoMapper;
import com.mp.generator.service.IProductInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ayetony
 * @since 2020-04-23
 */
@Service
public class ProductInfoServiceImpl extends ServiceImpl<ProductInfoMapper, ProductInfo> implements IProductInfoService {

}
