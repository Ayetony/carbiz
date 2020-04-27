package com.mp.generator.controller;


import com.mp.generator.utils.HttpClientPuller;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>
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
//              return "id null error";
              request.setAttribute("message","id null error");
              request.getRequestDispatcher(request.getContextPath() + "/" + "index.jsp").forward(request,response);
              return;
          }

          if(queryId.length()>20){
//              return  "id error long";
              request.setAttribute("message","id error long");
              request.getRequestDispatcher(  request.getContextPath() + "/" + "index.jsp").forward(request,response);
              return;
          }

        request.setAttribute("message",HttpClientPuller.getJsonByGetRequest(queryId));
        request.getRequestDispatcher(request.getContextPath() + "/" + "index.jsp").forward(request,response);
        //          return HttpClientPuller.getJsonByGetRequest(queryId);
    }




}
