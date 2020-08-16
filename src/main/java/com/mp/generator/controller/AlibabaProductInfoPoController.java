package com.mp.generator.controller;


import com.mp.generator.utils.HttpClientTaobaoCatPicker;
import com.mp.generator.utils.HttpClientTaobaoProductPuller;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 产品采集信息表 前端控制器
 * </p>
 *
 * @author ayetony
 * @since 2020-04-28
 */
@RestController
@RequestMapping("/generator/alibabaProductInfoPo")
public class AlibabaProductInfoPoController {

    @RequestMapping(value="/taobao_sku", method= RequestMethod.POST)
    public String queryAllProductInfoId(@RequestParam("taobao_id") String id){
        //HttpClientPuller.getJsonByGetRequest(queryId)
        if(StringUtils.isBlank(id)){
            return "Error ID";
        }
        return HttpClientTaobaoProductPuller.getJsonByGetRequest(id).toString();
    }

    @RequestMapping(value="/taobao_cat", method= RequestMethod.POST)
    public String queryTaobaoCat(@RequestParam("taobao_id") String id){
        if(StringUtils.isBlank(id)){
            return "Error ID";
        }
        return HttpClientTaobaoCatPicker.getJsonByGetRequest(id).toString();
    }

}
