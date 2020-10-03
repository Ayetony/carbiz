package com.mp.generator.controller;


import com.mp.generator.service.IHotProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author ayetony
 * @since 2020-09-29
 */
@RestController
@RequestMapping("/generator/hotProduct")
public class HotProductController {

    @Autowired
    IHotProductService productService ;

    @RequestMapping(value = "/hot_API", method= RequestMethod.POST)
    public String  extractDataAPIByHot(){
        return  productService.getEntityByHotProductsJSON();
    }

}
