package com.mp.generator.controller;


import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mp.generator.entity.AlibabaProductInfoPo;
import com.mp.generator.utils.HttpClientProductPuller;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
        //HttpClientPuller.getJsonByGetRequest(queryId)
        HttpClientProductPuller puller = new HttpClientProductPuller();
        Map.Entry<AlibabaProductInfoPo, Multimap<String,String>> map =  puller.productInfoFromJson(queryId).entrySet().iterator().next();

        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        String pro = gson.toJson(map.getKey());
        String mul = gson.toJson(map.getValue());

        request.setAttribute("message",gson.toJson(pro+mul));
        request.getRequestDispatcher(request.getContextPath() + "/" + "index.jsp").forward(request,response);
    }




}
