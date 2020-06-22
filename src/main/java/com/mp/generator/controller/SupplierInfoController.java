package com.mp.generator.controller;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mp.generator.entity.ProductPojo;
import com.mp.generator.utils.HttpClientSearchProduct;
import com.mp.generator.utils.HttpClientSupplierSearch;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author ayetony
 * @since 2020-05-01
 */
@RestController
@RequestMapping("/generator/supplierInfo")
public class SupplierInfoController {

    @RequestMapping(value="/search_sup_pro", method= RequestMethod.POST)
    public String searchAlibabaProducts(@RequestParam("keyword") String keyword, @RequestParam("page")Integer page){

        HttpClientSupplierSearch supplierSearch = new HttpClientSupplierSearch();
        return supplierSearch.search(keyword,page);
    }

}
