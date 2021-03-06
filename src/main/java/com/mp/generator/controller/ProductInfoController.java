package com.mp.generator.controller;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mp.generator.entity.AlibabaProductInfoPo;
import com.mp.generator.entity.ProductPojo;
import com.mp.generator.mapper.AlibabaProductInfoPoMapper;
import com.mp.generator.utils.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 // * <p>
 *  前端控制器
 * </p>
 *
 * @author ayetony
 * @since 2020-04-23
 */
@RestController
@RequestMapping("/generator/productInfo")
public class ProductInfoController {

    @Autowired
    AlibabaProductInfoPoMapper alibabaProductInfoPoMapper;

    @RequestMapping(value="/query_id", method= RequestMethod.POST)
    public void queryByProId(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String queryId = request.getParameter("query");
        if(StringUtils.isBlank(queryId)){
            request.setAttribute("message","id null error");
            request.getRequestDispatcher(request.getContextPath() + "/" + "index.jsp").forward(request,response);
            return;
        }
        if(queryId.length()>20){
            request.setAttribute("message","id error long");
            request.getRequestDispatcher(  request.getContextPath() + "/" + "index.jsp").forward(request,response);
            return;
        }
        String messageJson = HttpClientProductFullyPuller.getJsonByGetRequest(queryId,true);
        request.setAttribute("message",messageJson);
        request.getRequestDispatcher(request.getContextPath() + "/" + "index.jsp").forward(request,response);
    }

    @RequestMapping(value="/query_pro", method= RequestMethod.POST)
    public String queryAlibabaProductInfoId(@RequestParam("alibaba_product_id") String productId){
        //HttpClientPuller.getJsonByGetRequest(queryId)
        if(StringUtils.isBlank(productId)){
            return "Error ID";
        }
        HttpClientProductPuller puller = new HttpClientProductPuller();
        Map<AlibabaProductInfoPo,Multimap<String,String>> httpClientMap = puller.productInfoFromJson(productId);

        if(httpClientMap == null){
            return "not exist or single low-quality page";
        }
        Map.Entry<AlibabaProductInfoPo, Multimap<String,String>> map =  httpClientMap.entrySet().iterator().next();
        if(map == null){
            return "missing request";
        }
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        AlibabaProductInfoPo alibabaProductInfoPo = map.getKey();
        JsonType type = new JsonType();
        type.setAlibabaProductInfoPo(alibabaProductInfoPo);
        type.setSkus(map.getValue().asMap());
        return gson.toJson(type);
    }

    @RequestMapping(value="/excel/download", method = RequestMethod.POST)
    public ResponseEntity<FileSystemResource> downloadExcel(HttpServletResponse response) throws IOException {
        List<AlibabaProductInfoPo>  productInfoPoList =  alibabaProductInfoPoMapper.
                selectList(new QueryWrapper<AlibabaProductInfoPo>()
                        .gt("id",450000).and(Wrapper -> Wrapper.lt("id",500000)));
        File file = ExcelProcess.itemsSkuToExcel(productInfoPoList);
        response.setContentType("multipart/form-data;charset=UTF-8");
        return  excelInfo(file);
    }


    @RequestMapping(value="/search_pro", method= RequestMethod.POST)
    public String searchAlibabaProducts(@RequestParam("keyword") String keyword,@RequestParam("page")Integer page){
        HttpClientSearchProduct puller = new HttpClientSearchProduct();
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        List<ProductPojo> pojos = puller.generatePojos(keyword,page);
        return gson.toJson(pojos);
    }


    public ResponseEntity<FileSystemResource> excelInfo(File file) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Disposition", "attachment; filename=" + file.getName() + ".xlsx");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add("Last-Modified", new Date().toString());
        headers.add("ETag", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok().headers(headers).contentLength(file.length()).contentType(MediaType.parseMediaType("application/octet-stream")).body(new FileSystemResource(file));
    }

    @RequestMapping(value="/query_all_id", method= RequestMethod.POST)
    public String queryAllProductInfoId(@RequestParam("alibaba_product_id") String id){
        //HttpClientPuller.getJsonByGetRequest(queryId)
        if(StringUtils.isBlank(id)){
            return "Error ID";
        }
        HttpClientDetailProductPuller puller = new HttpClientDetailProductPuller();
        Map<AlibabaProductInfoPo,Multimap<String,String>> httpClientMap = puller.productInfoFromJson(id);

        if(httpClientMap == null){
            return "not exist";
        }

        Map.Entry<AlibabaProductInfoPo, Multimap<String,String>> map =  httpClientMap.entrySet().iterator().next();

        if(map == null){
            return "missing request";
        }
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        AlibabaProductInfoPo alibabaProductInfoPo = map.getKey();
        JsonType type = new JsonType();
        type.setAlibabaProductInfoPo(alibabaProductInfoPo);
        type.setSkus(map.getValue().asMap());
        return gson.toJson(type);
    }

    @RequestMapping(value="/query_fully_all", method= RequestMethod.POST)
    public String queryFullyProductInfoId(@RequestParam("alibaba_product_id") String id){

        if(StringUtils.isBlank(id)){
            return "Error ID";
        }
        return HttpClientProductFullyPuller.getJsonByGetRequest(id,true);
    }


    @RequestMapping(value="/search_shopee_pro", method= RequestMethod.POST)
    public String searchShopeeProducts(@RequestParam("keyword") String keyword,@RequestParam("page")Integer page){

        HttpClientShopeeSearchPuller shopeeSearchPuller = new HttpClientShopeeSearchPuller();
        return shopeeSearchPuller.purify(shopeeSearchPuller.search(keyword,page));
    }

    @RequestMapping(value="/shopee_pro", method= RequestMethod.POST)
    public String  queryShopeeProduct(@RequestParam("shopee_id") String shopeeID){
        return HttpClientShopeeProPuller.getJsonByGetRequest(shopeeID).toString();
    }

    @PostMapping("/order_looking")
    public String orderInfoLooking(@RequestParam("sku_img_link") String skuIMGLink,@RequestParam("1688_product_id") String id){

        HttpClientProductFullyPuller.getJsonByGetRequest(id,true);
        return null;

    }




}
