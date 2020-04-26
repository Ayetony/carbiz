package com.mp.generator.utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mp.generator.entity.AlibabaProductInfoPo;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpClientPuller {

    public static String getJsonByGetRequest(String id) {

         CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        String oneBoundApi = "https://api.onebound.cn/1688/api_call.php?key=tel18606528273&secret=20200417&api_name=item_get&num_iid=";
        HttpPost httpPost = new HttpPost(oneBoundApi + id);
         // 响应模型
         CloseableHttpResponse response = null;
         String responseStr = "";
         try {
             // 由客户端执行(发送)Get请求
             response = httpClient.execute(httpPost);
             // 从响应模型中获取响应实体
             HttpEntity responseEntity = response.getEntity();
             System.out.println("响应状态为:" + response.getStatusLine());
             if (responseEntity != null) {
                 responseStr = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
             }
             System.out.println(purify(responseStr));
         } catch (ParseException | IOException e) {
             e.printStackTrace();
         } finally {
             try {
                 // 释放资源
                 if (httpClient != null) {
                     httpClient.close();
                 }
                 if (response != null) {
                     response.close();
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         return purify(responseStr);
     }

    public static void main(String[] args) {
//        String responseStr = new HttpClientPuller().getJsonByGetRequest("533816674053");
        JsonParser parser = new JsonParser();
        JsonElement element;
        element = parser.parse(json);

        AlibabaProductInfoPo productInfoPo = new AlibabaProductInfoPo();

        Map<String,String> propImgMap  = mapJson(element,"props_img");
        Map<String,String> propListMap = mapJson(element,"props_list");

        Map<String,String> imgPropMap = new HashMap<>();
        propImgMap.forEach((key, value) -> imgPropMap.put(propListMap.get(key), value));

        String packageWeight =  "packageWeight:" + element.getAsJsonObject().get("m_weight").getAsString();
        String unitWeight = "unitWeight:" + element.getAsJsonObject().get("j_weight").getAsString();
        String volume = "volume:" + element.getAsJsonObject().get("volume").getAsString();

        String crossBorderPro = packageWeight + ";" + unitWeight + ";" + volume;



        String productImgLink = element.getAsJsonObject().get("pic_url").getAsString();
        String productName = element.getAsJsonObject().get("title").getAsString();
        String totalSaleThisMonth = element.getAsJsonObject().get("sales").getAsString();
        String deliveryAddress = element.getAsJsonObject().get("location").getAsString();
        JsonObject sellerInfo = element.getAsJsonObject().get("seller_info").getAsJsonObject();
        String shopRef =  sellerInfo.get("zhuy").getAsString();
        String shopName = sellerInfo.get("shop_name").getAsString();
        String priceRange = element.getAsJsonObject().get("priceRange").toString();
        String min_num = element.getAsJsonObject().get("min_num").getAsString();
        String basic_price = element.getAsJsonObject().get("price").getAsString();
        String currentPrice;
        if(StringUtils.isNotBlank(priceRange)){
            currentPrice = priceRange;
        }else{
            currentPrice = "["+min_num + "," + basic_price + "]";
        }

        String  productLink = element.getAsJsonObject().get("detail_url").getAsString();

        productInfoPo.setProductDetail(productLink);
        productInfoPo.setCrossBorderPro(crossBorderPro);
        productInfoPo.setProductImgLink(productImgLink);
        productInfoPo.setCurrentPrice(currentPrice);
        productInfoPo.setShopRef(shopRef);
        productInfoPo.setDeliveryAddress(deliveryAddress);
        productInfoPo.setTotalSaleThisMonth(totalSaleThisMonth);
        productInfoPo.setShopName(shopName);
        productInfoPo.setProductName(productName);


        Multimap<String,String> skus = ArrayListMultimap.create();


        element.getAsJsonObject().get("skus").getAsJsonObject().get("sku").
                getAsJsonArray().forEach( e -> {
                   String name = e.getAsJsonObject().get("properties_name").getAsString();
                   String[] sku = name.split(";");
                   String color = StringUtils.substring(sku[0],sku[0].lastIndexOf("颜色:"));
                   String imgURL = imgPropMap.get(color);
                   String size = StringUtils.substring(sku[1],sku[1].lastIndexOf("尺码:"));
                   String price = "price:" + e.getAsJsonObject().get("price").getAsString();
                   String quantity = "quantity:" + e.getAsJsonObject().get("quantity").getAsString();
                   skus.put(imgURL,size.replace("尺码","size") + ";"+ color.replace("颜色","color") + ";" + price + ";" + quantity);
        } );

        skus.entries().forEach(HttpClientPuller::accept);



    }

    private static String  purify(String json){
        JsonParser parser = new JsonParser();
        JsonElement element =  parser.parse(json).getAsJsonObject().get("item");
        if(element.isJsonNull()){
            return "no value";
        }
        return  element.toString().replace("num_iid","product_id");
    }

    private static Map<String,String> mapJson(JsonElement element,String key){
        JsonElement keyElement = element.getAsJsonObject().get(key);
        Map<String,String> hashMap = new HashMap<>();
        keyElement.getAsJsonObject().entrySet().forEach( e -> hashMap.put(e.getKey().replace("\"",""),e.getValue().toString().replace("\"","")));
        return hashMap;
    }



    private static String json = "{\n" +
            "\t\"product_id\": \"548544300935\",\n" +
            "\t\"title\": \"2019夏季厂家一件代发新款莫代尔网红背心 带胸垫吊带小背心女\",\n" +
            "\t\"desc_short\": \"\",\n" +
            "\t\"price\": \"18.00\",\n" +
            "\t\"total_price\": 0,\n" +
            "\t\"suggestive_price\": 0,\n" +
            "\t\"orginal_price\": \"18.00\",\n" +
            "\t\"nick\": \"迈捷制衣\",\n" +
            "\t\"num\": \"9193\",\n" +
            "\t\"min_num\": 5,\n" +
            "\t\"detail_url\": \"https://detail.1688.com/offer/548544300935.html\",\n" +
            "\t\"pic_url\": \"https://cbu01.alicdn.com/img/ibank/2017/962/054/4078450269_759960632.jpg\",\n" +
            "\t\"brand\": \"\",\n" +
            "\t\"brandId\": \"\",\n" +
            "\t\"rootCatId\": \"\",\n" +
            "\t\"cid\": \"1031920\",\n" +
            "\t\"favcount\": 0,\n" +
            "\t\"fanscount\": 0,\n" +
            "\t\"crumbs\": [],\n" +
            "\t\"created_time\": \"\",\n" +
            "\t\"modified_time\": \"\",\n" +
            "\t\"delist_time\": \"\",\n" +
            "\t\"desc\": \"<div id=\\\"offer-template-0\\\"></div><div>SHOPTOOL_POSITION_TOP_BEGIN</div><div>SHOPTOOL_关联营销_302806_BEGIN</div><table border=\\\"0\\\" cellpadding=\\\"0\\\" cellspacing=\\\"0\\\" style=\\\"width: 100.0%;\\\"><tr><td style=\\\"font-size: 0;height: 5.0px;\\\">&nbsp;</td></tr><tr><td align=\\\"center\\\" background=\\\"https://cbu01.alicdn.com/img/ibank/2016/004/039/3228930400_1624713353.jpg\\\" style=\\\"height: 80.0px;\\\"><div style=\\\"max-width: 315.0px;margin: auto;\\\"><img src=\\\"https://img.alicdn.com/L1/249/15057832094931/1.0.0/img/15407822713233.png\\\" width=\\\"100%\\\" /></div></td></tr><tr><td style=\\\"font-size: 0;height: 10.0px;\\\">&nbsp;</td></tr></table><table border=\\\"0\\\" cellpadding=\\\"0\\\" cellspacing=\\\"0\\\" style=\\\"width: 100.0%;\\\"><tr><td align=\\\"center\\\" style=\\\"border-left: 1.0px solid #c9acac;border-right: 1.0px solid #c9acac;border-top: 1.0px solid #c9acac;\\\" width=\\\"24.25%\\\"><a href=\\\"https://detail.1688.com/offer/612216451409.html\\\" style=\\\"display: block;width: 100.0%;\\\" target=\\\"_black\\\"><img src=\\\"https://cbu01.alicdn.com/img/ibank/2020/673/741/13376147376_759960632.310x310.jpg\\\" width=\\\"100%\\\" /></a></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"border-left: 1.0px solid #c9acac;border-right: 1.0px solid #c9acac;border-top: 1.0px solid #c9acac;\\\" width=\\\"24.25%\\\"><a href=\\\"https://detail.1688.com/offer/612189458405.html\\\" style=\\\"display: block;width: 100.0%;\\\" target=\\\"_black\\\"><img src=\\\"https://cbu01.alicdn.com/img/ibank/2020/539/561/13376165935_759960632.310x310.jpg\\\" width=\\\"100%\\\" /></a></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"border-left: 1.0px solid #c9acac;border-right: 1.0px solid #c9acac;border-top: 1.0px solid #c9acac;\\\" width=\\\"24.25%\\\"><a href=\\\"https://detail.1688.com/offer/611677260295.html\\\" style=\\\"display: block;width: 100.0%;\\\" target=\\\"_black\\\"><img src=\\\"https://cbu01.alicdn.com/img/ibank/2020/584/597/13293795485_759960632.310x310.jpg\\\" width=\\\"100%\\\" /></a></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"border-left: 1.0px solid #c9acac;border-right: 1.0px solid #c9acac;border-top: 1.0px solid #c9acac;\\\" width=\\\"24.25%\\\"><a href=\\\"https://detail.1688.com/offer/611945685340.html\\\" style=\\\"display: block;width: 100.0%;\\\" target=\\\"_black\\\"><img src=\\\"https://cbu01.alicdn.com/img/ibank/2020/448/872/13331278844_759960632.310x310.jpg\\\" width=\\\"100%\\\" /></a></td></tr><tr><td align=\\\"center\\\" style=\\\"border-left: 1.0px solid #c9acac;border-right: 1.0px solid #c9acac;border-bottom: 1.0px solid #c9acac;\\\" width=\\\"24.25%\\\"><table border=\\\"0\\\" cellpadding=\\\"0\\\" cellspacing=\\\"0\\\" style=\\\"width: 100.0%;\\\"><tr><td align=\\\"center\\\" style=\\\"background-color: #ffffff;font-family: 微软雅黑;font-size: 12.0px;color: #000000;\\\"><span style=\\\"display: block;padding: 3.0% 0;\\\">￥16.5</span></td></tr><tr><td align=\\\"center\\\" style=\\\"height: 20.0px;font-size: 0.0px;\\\">&nbsp;</td></tr></table></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"border-left: 1.0px solid #c9acac;border-right: 1.0px solid #c9acac;border-bottom: 1.0px solid #c9acac;\\\" width=\\\"24.25%\\\"><table border=\\\"0\\\" cellpadding=\\\"0\\\" cellspacing=\\\"0\\\" style=\\\"width: 100.0%;\\\"><tr><td align=\\\"center\\\" style=\\\"background-color: #ffffff;font-family: 微软雅黑;font-size: 12.0px;color: #000000;\\\"><span style=\\\"display: block;padding: 3.0% 0;\\\">￥15.5</span></td></tr><tr><td align=\\\"center\\\" style=\\\"height: 20.0px;font-size: 0.0px;\\\">&nbsp;</td></tr></table></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"border-left: 1.0px solid #c9acac;border-right: 1.0px solid #c9acac;border-bottom: 1.0px solid #c9acac;\\\" width=\\\"24.25%\\\"><table border=\\\"0\\\" cellpadding=\\\"0\\\" cellspacing=\\\"0\\\" style=\\\"width: 100.0%;\\\"><tr><td align=\\\"center\\\" style=\\\"background-color: #ffffff;font-family: 微软雅黑;font-size: 12.0px;color: #000000;\\\"><span style=\\\"display: block;padding: 3.0% 0;\\\">￥13.5</span></td></tr><tr><td align=\\\"center\\\" style=\\\"height: 20.0px;font-size: 0.0px;\\\">&nbsp;</td></tr></table></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"border-left: 1.0px solid #c9acac;border-right: 1.0px solid #c9acac;border-bottom: 1.0px solid #c9acac;\\\" width=\\\"24.25%\\\"><table border=\\\"0\\\" cellpadding=\\\"0\\\" cellspacing=\\\"0\\\" style=\\\"width: 100.0%;\\\"><tr><td align=\\\"center\\\" style=\\\"background-color: #ffffff;font-family: 微软雅黑;font-size: 12.0px;color: #000000;\\\"><span style=\\\"display: block;padding: 3.0% 0;\\\">￥16.5</span></td></tr><tr><td align=\\\"center\\\" style=\\\"height: 20.0px;font-size: 0.0px;\\\">&nbsp;</td></tr></table></td></tr><tr><td align=\\\"center\\\" style=\\\"height: 5.0px;\\\" width=\\\"24.25%\\\"><a href=\\\"https://detail.1688.com/offer/612216451409.html\\\" style=\\\"margin-top: -24.0px;display: block;height: 100.0%;width: 36.0px;\\\" target=\\\"_black\\\"><img src=\\\"https://img.alicdn.com/L1/249/14641382983631/1.0.0/img/14684020340080.png\\\" width=\\\"100%\\\" /></a></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"height: 5.0px;\\\" width=\\\"24.25%\\\"><a href=\\\"https://detail.1688.com/offer/612189458405.html\\\" style=\\\"margin-top: -24.0px;display: block;height: 100.0%;width: 36.0px;\\\" target=\\\"_black\\\"><img src=\\\"https://img.alicdn.com/L1/249/14641382983631/1.0.0/img/14684020340080.png\\\" width=\\\"100%\\\" /></a></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"height: 5.0px;\\\" width=\\\"24.25%\\\"><a href=\\\"https://detail.1688.com/offer/611677260295.html\\\" style=\\\"margin-top: -24.0px;display: block;height: 100.0%;width: 36.0px;\\\" target=\\\"_black\\\"><img src=\\\"https://img.alicdn.com/L1/249/14641382983631/1.0.0/img/14684020340080.png\\\" width=\\\"100%\\\" /></a></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"height: 5.0px;\\\" width=\\\"24.25%\\\"><a href=\\\"https://detail.1688.com/offer/611945685340.html\\\" style=\\\"margin-top: -24.0px;display: block;height: 100.0%;width: 36.0px;\\\" target=\\\"_black\\\"><img src=\\\"https://img.alicdn.com/L1/249/14641382983631/1.0.0/img/14684020340080.png\\\" width=\\\"100%\\\" /></a></td></tr><tr><td colspan=\\\"7\\\" style=\\\"font-size: 0;height: 10.0px;\\\">&nbsp;</td></tr><tr><td align=\\\"center\\\" style=\\\"border-left: 1.0px solid #c9acac;border-right: 1.0px solid #c9acac;border-top: 1.0px solid #c9acac;\\\" width=\\\"24.25%\\\"><a href=\\\"https://detail.1688.com/offer/612217627249.html\\\" style=\\\"display: block;width: 100.0%;\\\" target=\\\"_black\\\"><img src=\\\"https://cbu01.alicdn.com/img/ibank/2020/685/981/13376189586_759960632.310x310.jpg\\\" width=\\\"100%\\\" /></a></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"border-left: 1.0px solid #c9acac;border-right: 1.0px solid #c9acac;border-top: 1.0px solid #c9acac;\\\" width=\\\"24.25%\\\"><a href=\\\"https://detail.1688.com/offer/611944585489.html\\\" style=\\\"display: block;width: 100.0%;\\\" target=\\\"_black\\\"><img src=\\\"https://cbu01.alicdn.com/img/ibank/2020/954/681/13376186459_759960632.310x310.jpg\\\" width=\\\"100%\\\" /></a></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"border-left: 1.0px solid #c9acac;border-right: 1.0px solid #c9acac;border-top: 1.0px solid #c9acac;\\\" width=\\\"24.25%\\\"><a href=\\\"https://detail.1688.com/offer/612190326266.html\\\" style=\\\"display: block;width: 100.0%;\\\" target=\\\"_black\\\"><img src=\\\"https://cbu01.alicdn.com/img/ibank/2020/324/321/13376123423_759960632.310x310.jpg\\\" width=\\\"100%\\\" /></a></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"border-left: 1.0px solid #c9acac;border-right: 1.0px solid #c9acac;border-top: 1.0px solid #c9acac;\\\" width=\\\"24.25%\\\"><a href=\\\"https://detail.1688.com/offer/611734157286.html\\\" style=\\\"display: block;width: 100.0%;\\\" target=\\\"_black\\\"><img src=\\\"https://cbu01.alicdn.com/img/ibank/2020/155/025/13375520551_759960632.310x310.jpg\\\" width=\\\"100%\\\" /></a></td></tr><tr><td align=\\\"center\\\" style=\\\"border-left: 1.0px solid #c9acac;border-right: 1.0px solid #c9acac;border-bottom: 1.0px solid #c9acac;\\\" width=\\\"24.25%\\\"><table border=\\\"0\\\" cellpadding=\\\"0\\\" cellspacing=\\\"0\\\" style=\\\"width: 100.0%;\\\"><tr><td align=\\\"center\\\" style=\\\"background-color: #ffffff;font-family: 微软雅黑;font-size: 12.0px;color: #000000;\\\"><span style=\\\"display: block;padding: 3.0% 0;\\\">￥16.5</span></td></tr><tr><td align=\\\"center\\\" style=\\\"height: 20.0px;font-size: 0.0px;\\\">&nbsp;</td></tr></table></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"border-left: 1.0px solid #c9acac;border-right: 1.0px solid #c9acac;border-bottom: 1.0px solid #c9acac;\\\" width=\\\"24.25%\\\"><table border=\\\"0\\\" cellpadding=\\\"0\\\" cellspacing=\\\"0\\\" style=\\\"width: 100.0%;\\\"><tr><td align=\\\"center\\\" style=\\\"background-color: #ffffff;font-family: 微软雅黑;font-size: 12.0px;color: #000000;\\\"><span style=\\\"display: block;padding: 3.0% 0;\\\">￥16.5</span></td></tr><tr><td align=\\\"center\\\" style=\\\"height: 20.0px;font-size: 0.0px;\\\">&nbsp;</td></tr></table></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"border-left: 1.0px solid #c9acac;border-right: 1.0px solid #c9acac;border-bottom: 1.0px solid #c9acac;\\\" width=\\\"24.25%\\\"><table border=\\\"0\\\" cellpadding=\\\"0\\\" cellspacing=\\\"0\\\" style=\\\"width: 100.0%;\\\"><tr><td align=\\\"center\\\" style=\\\"background-color: #ffffff;font-family: 微软雅黑;font-size: 12.0px;color: #000000;\\\"><span style=\\\"display: block;padding: 3.0% 0;\\\">￥16.5</span></td></tr><tr><td align=\\\"center\\\" style=\\\"height: 20.0px;font-size: 0.0px;\\\">&nbsp;</td></tr></table></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"border-left: 1.0px solid #c9acac;border-right: 1.0px solid #c9acac;border-bottom: 1.0px solid #c9acac;\\\" width=\\\"24.25%\\\"><table border=\\\"0\\\" cellpadding=\\\"0\\\" cellspacing=\\\"0\\\" style=\\\"width: 100.0%;\\\"><tr><td align=\\\"center\\\" style=\\\"background-color: #ffffff;font-family: 微软雅黑;font-size: 12.0px;color: #000000;\\\"><span style=\\\"display: block;padding: 3.0% 0;\\\">￥18.5</span></td></tr><tr><td align=\\\"center\\\" style=\\\"height: 20.0px;font-size: 0.0px;\\\">&nbsp;</td></tr></table></td></tr><tr><td align=\\\"center\\\" style=\\\"height: 5.0px;\\\" width=\\\"24.25%\\\"><a href=\\\"https://detail.1688.com/offer/612217627249.html\\\" style=\\\"margin-top: -24.0px;display: block;height: 100.0%;width: 36.0px;\\\" target=\\\"_black\\\"><img src=\\\"https://img.alicdn.com/L1/249/14641382983631/1.0.0/img/14684020340080.png\\\" width=\\\"100%\\\" /></a></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"height: 5.0px;\\\" width=\\\"24.25%\\\"><a href=\\\"https://detail.1688.com/offer/611944585489.html\\\" style=\\\"margin-top: -24.0px;display: block;height: 100.0%;width: 36.0px;\\\" target=\\\"_black\\\"><img src=\\\"https://img.alicdn.com/L1/249/14641382983631/1.0.0/img/14684020340080.png\\\" width=\\\"100%\\\" /></a></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"height: 5.0px;\\\" width=\\\"24.25%\\\"><a href=\\\"https://detail.1688.com/offer/612190326266.html\\\" style=\\\"margin-top: -24.0px;display: block;height: 100.0%;width: 36.0px;\\\" target=\\\"_black\\\"><img src=\\\"https://img.alicdn.com/L1/249/14641382983631/1.0.0/img/14684020340080.png\\\" width=\\\"100%\\\" /></a></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"height: 5.0px;\\\" width=\\\"24.25%\\\"><a href=\\\"https://detail.1688.com/offer/611734157286.html\\\" style=\\\"margin-top: -24.0px;display: block;height: 100.0%;width: 36.0px;\\\" target=\\\"_black\\\"><img src=\\\"https://img.alicdn.com/L1/249/14641382983631/1.0.0/img/14684020340080.png\\\" width=\\\"100%\\\" /></a></td></tr><tr><td colspan=\\\"7\\\" style=\\\"font-size: 0;height: 10.0px;\\\">&nbsp;</td></tr><tr><td align=\\\"center\\\" style=\\\"border-left: 1.0px solid #c9acac;border-right: 1.0px solid #c9acac;border-top: 1.0px solid #c9acac;\\\" width=\\\"24.25%\\\"><a href=\\\"https://detail.1688.com/offer/596198953013.html\\\" style=\\\"display: block;width: 100.0%;\\\" target=\\\"_black\\\"><img src=\\\"https://cbu01.alicdn.com/img/ibank/2019/549/455/11698554945_759960632.310x310.jpg\\\" width=\\\"100%\\\" /></a></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"border-left: 1.0px solid #c9acac;border-right: 1.0px solid #c9acac;border-top: 1.0px solid #c9acac;\\\" width=\\\"24.25%\\\"><a href=\\\"https://detail.1688.com/offer/612429255429.html\\\" style=\\\"display: block;width: 100.0%;\\\" target=\\\"_black\\\"><img src=\\\"https://cbu01.alicdn.com/img/ibank/2020/078/835/13375538870_759960632.310x310.jpg\\\" width=\\\"100%\\\" /></a></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"border-left: 1.0px solid #c9acac;border-right: 1.0px solid #c9acac;border-top: 1.0px solid #c9acac;\\\" width=\\\"24.25%\\\"><a href=\\\"https://detail.1688.com/offer/612125941791.html\\\" style=\\\"display: block;width: 100.0%;\\\" target=\\\"_black\\\"><img src=\\\"https://cbu01.alicdn.com/img/ibank/2020/348/415/13395514843_759960632.310x310.jpg\\\" width=\\\"100%\\\" /></a></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"border-left: 1.0px solid #c9acac;border-right: 1.0px solid #c9acac;border-top: 1.0px solid #c9acac;\\\" width=\\\"24.25%\\\"><a href=\\\"https://detail.1688.com/offer/611734273327.html\\\" style=\\\"display: block;width: 100.0%;\\\" target=\\\"_black\\\"><img src=\\\"https://cbu01.alicdn.com/img/ibank/2020/627/471/13376174726_759960632.310x310.jpg\\\" width=\\\"100%\\\" /></a></td></tr><tr><td align=\\\"center\\\" style=\\\"border-left: 1.0px solid #c9acac;border-right: 1.0px solid #c9acac;border-bottom: 1.0px solid #c9acac;\\\" width=\\\"24.25%\\\"><table border=\\\"0\\\" cellpadding=\\\"0\\\" cellspacing=\\\"0\\\" style=\\\"width: 100.0%;\\\"><tr><td align=\\\"center\\\" style=\\\"background-color: #ffffff;font-family: 微软雅黑;font-size: 12.0px;color: #000000;\\\"><span style=\\\"display: block;padding: 3.0% 0;\\\">￥17.5</span></td></tr><tr><td align=\\\"center\\\" style=\\\"height: 20.0px;font-size: 0.0px;\\\">&nbsp;</td></tr></table></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"border-left: 1.0px solid #c9acac;border-right: 1.0px solid #c9acac;border-bottom: 1.0px solid #c9acac;\\\" width=\\\"24.25%\\\"><table border=\\\"0\\\" cellpadding=\\\"0\\\" cellspacing=\\\"0\\\" style=\\\"width: 100.0%;\\\"><tr><td align=\\\"center\\\" style=\\\"background-color: #ffffff;font-family: 微软雅黑;font-size: 12.0px;color: #000000;\\\"><span style=\\\"display: block;padding: 3.0% 0;\\\">￥25</span></td></tr><tr><td align=\\\"center\\\" style=\\\"height: 20.0px;font-size: 0.0px;\\\">&nbsp;</td></tr></table></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"border-left: 1.0px solid #c9acac;border-right: 1.0px solid #c9acac;border-bottom: 1.0px solid #c9acac;\\\" width=\\\"24.25%\\\"><table border=\\\"0\\\" cellpadding=\\\"0\\\" cellspacing=\\\"0\\\" style=\\\"width: 100.0%;\\\"><tr><td align=\\\"center\\\" style=\\\"background-color: #ffffff;font-family: 微软雅黑;font-size: 12.0px;color: #000000;\\\"><span style=\\\"display: block;padding: 3.0% 0;\\\">￥25</span></td></tr><tr><td align=\\\"center\\\" style=\\\"height: 20.0px;font-size: 0.0px;\\\">&nbsp;</td></tr></table></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"border-left: 1.0px solid #c9acac;border-right: 1.0px solid #c9acac;border-bottom: 1.0px solid #c9acac;\\\" width=\\\"24.25%\\\"><table border=\\\"0\\\" cellpadding=\\\"0\\\" cellspacing=\\\"0\\\" style=\\\"width: 100.0%;\\\"><tr><td align=\\\"center\\\" style=\\\"background-color: #ffffff;font-family: 微软雅黑;font-size: 12.0px;color: #000000;\\\"><span style=\\\"display: block;padding: 3.0% 0;\\\">￥25</span></td></tr><tr><td align=\\\"center\\\" style=\\\"height: 20.0px;font-size: 0.0px;\\\">&nbsp;</td></tr></table></td></tr><tr><td align=\\\"center\\\" style=\\\"height: 5.0px;\\\" width=\\\"24.25%\\\"><a href=\\\"https://detail.1688.com/offer/596198953013.html\\\" style=\\\"margin-top: -24.0px;display: block;height: 100.0%;width: 36.0px;\\\" target=\\\"_black\\\"><img src=\\\"https://img.alicdn.com/L1/249/14641382983631/1.0.0/img/14684020340080.png\\\" width=\\\"100%\\\" /></a></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"height: 5.0px;\\\" width=\\\"24.25%\\\"><a href=\\\"https://detail.1688.com/offer/612429255429.html\\\" style=\\\"margin-top: -24.0px;display: block;height: 100.0%;width: 36.0px;\\\" target=\\\"_black\\\"><img src=\\\"https://img.alicdn.com/L1/249/14641382983631/1.0.0/img/14684020340080.png\\\" width=\\\"100%\\\" /></a></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"height: 5.0px;\\\" width=\\\"24.25%\\\"><a href=\\\"https://detail.1688.com/offer/612125941791.html\\\" style=\\\"margin-top: -24.0px;display: block;height: 100.0%;width: 36.0px;\\\" target=\\\"_black\\\"><img src=\\\"https://img.alicdn.com/L1/249/14641382983631/1.0.0/img/14684020340080.png\\\" width=\\\"100%\\\" /></a></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"height: 5.0px;\\\" width=\\\"24.25%\\\"><a href=\\\"https://detail.1688.com/offer/611734273327.html\\\" style=\\\"margin-top: -24.0px;display: block;height: 100.0%;width: 36.0px;\\\" target=\\\"_black\\\"><img src=\\\"https://img.alicdn.com/L1/249/14641382983631/1.0.0/img/14684020340080.png\\\" width=\\\"100%\\\" /></a></td></tr><tr><td colspan=\\\"7\\\" style=\\\"font-size: 0;height: 10.0px;\\\">&nbsp;</td></tr><tr><td align=\\\"center\\\" style=\\\"border-left: 1.0px solid #c9acac;border-right: 1.0px solid #c9acac;border-top: 1.0px solid #c9acac;\\\" width=\\\"24.25%\\\"><a href=\\\"https://detail.1688.com/offer/610394720618.html\\\" style=\\\"display: block;width: 100.0%;\\\" target=\\\"_black\\\"><img src=\\\"https://cbu01.alicdn.com/img/ibank/2019/979/873/13080378979_759960632.310x310.jpg\\\" width=\\\"100%\\\" /></a></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"border-left: 1.0px solid #c9acac;border-right: 1.0px solid #c9acac;border-top: 1.0px solid #c9acac;\\\" width=\\\"24.25%\\\"><a href=\\\"https://detail.1688.com/offer/611944861496.html\\\" style=\\\"display: block;width: 100.0%;\\\" target=\\\"_black\\\"><img src=\\\"https://cbu01.alicdn.com/img/ibank/2020/489/432/13376234984_759960632.310x310.jpg\\\" width=\\\"100%\\\" /></a></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"border-left: 1.0px solid #c9acac;border-right: 1.0px solid #c9acac;border-top: 1.0px solid #c9acac;\\\" width=\\\"24.25%\\\"><a href=\\\"https://detail.1688.com/offer/600746080384.html\\\" style=\\\"display: block;width: 100.0%;\\\" target=\\\"_black\\\"><img src=\\\"https://cbu01.alicdn.com/img/ibank/2019/702/039/11766930207_759960632.310x310.jpg\\\" width=\\\"100%\\\" /></a></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"border-left: 1.0px solid #c9acac;border-right: 1.0px solid #c9acac;border-top: 1.0px solid #c9acac;\\\" width=\\\"24.25%\\\"><a href=\\\"https://detail.1688.com/offer/578537610605.html\\\" style=\\\"display: block;width: 100.0%;\\\" target=\\\"_black\\\"><img src=\\\"https://cbu01.alicdn.com/img/ibank/2019/026/934/11660439620_759960632.310x310.jpg\\\" width=\\\"100%\\\" /></a></td></tr><tr><td align=\\\"center\\\" style=\\\"border-left: 1.0px solid #c9acac;border-right: 1.0px solid #c9acac;border-bottom: 1.0px solid #c9acac;\\\" width=\\\"24.25%\\\"><table border=\\\"0\\\" cellpadding=\\\"0\\\" cellspacing=\\\"0\\\" style=\\\"width: 100.0%;\\\"><tr><td align=\\\"center\\\" style=\\\"background-color: #ffffff;font-family: 微软雅黑;font-size: 12.0px;color: #000000;\\\"><span style=\\\"display: block;padding: 3.0% 0;\\\">￥18.5</span></td></tr><tr><td align=\\\"center\\\" style=\\\"height: 20.0px;font-size: 0.0px;\\\">&nbsp;</td></tr></table></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"border-left: 1.0px solid #c9acac;border-right: 1.0px solid #c9acac;border-bottom: 1.0px solid #c9acac;\\\" width=\\\"24.25%\\\"><table border=\\\"0\\\" cellpadding=\\\"0\\\" cellspacing=\\\"0\\\" style=\\\"width: 100.0%;\\\"><tr><td align=\\\"center\\\" style=\\\"background-color: #ffffff;font-family: 微软雅黑;font-size: 12.0px;color: #000000;\\\"><span style=\\\"display: block;padding: 3.0% 0;\\\">￥19</span></td></tr><tr><td align=\\\"center\\\" style=\\\"height: 20.0px;font-size: 0.0px;\\\">&nbsp;</td></tr></table></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"border-left: 1.0px solid #c9acac;border-right: 1.0px solid #c9acac;border-bottom: 1.0px solid #c9acac;\\\" width=\\\"24.25%\\\"><table border=\\\"0\\\" cellpadding=\\\"0\\\" cellspacing=\\\"0\\\" style=\\\"width: 100.0%;\\\"><tr><td align=\\\"center\\\" style=\\\"background-color: #ffffff;font-family: 微软雅黑;font-size: 12.0px;color: #000000;\\\"><span style=\\\"display: block;padding: 3.0% 0;\\\">￥26.8</span></td></tr><tr><td align=\\\"center\\\" style=\\\"height: 20.0px;font-size: 0.0px;\\\">&nbsp;</td></tr></table></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"border-left: 1.0px solid #c9acac;border-right: 1.0px solid #c9acac;border-bottom: 1.0px solid #c9acac;\\\" width=\\\"24.25%\\\"><table border=\\\"0\\\" cellpadding=\\\"0\\\" cellspacing=\\\"0\\\" style=\\\"width: 100.0%;\\\"><tr><td align=\\\"center\\\" style=\\\"background-color: #ffffff;font-family: 微软雅黑;font-size: 12.0px;color: #000000;\\\"><span style=\\\"display: block;padding: 3.0% 0;\\\">￥24.5</span></td></tr><tr><td align=\\\"center\\\" style=\\\"height: 20.0px;font-size: 0.0px;\\\">&nbsp;</td></tr></table></td></tr><tr><td align=\\\"center\\\" style=\\\"height: 5.0px;\\\" width=\\\"24.25%\\\"><a href=\\\"https://detail.1688.com/offer/610394720618.html\\\" style=\\\"margin-top: -24.0px;display: block;height: 100.0%;width: 36.0px;\\\" target=\\\"_black\\\"><img src=\\\"https://img.alicdn.com/L1/249/14641382983631/1.0.0/img/14684020340080.png\\\" width=\\\"100%\\\" /></a></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"height: 5.0px;\\\" width=\\\"24.25%\\\"><a href=\\\"https://detail.1688.com/offer/611944861496.html\\\" style=\\\"margin-top: -24.0px;display: block;height: 100.0%;width: 36.0px;\\\" target=\\\"_black\\\"><img src=\\\"https://img.alicdn.com/L1/249/14641382983631/1.0.0/img/14684020340080.png\\\" width=\\\"100%\\\" /></a></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"height: 5.0px;\\\" width=\\\"24.25%\\\"><a href=\\\"https://detail.1688.com/offer/600746080384.html\\\" style=\\\"margin-top: -24.0px;display: block;height: 100.0%;width: 36.0px;\\\" target=\\\"_black\\\"><img src=\\\"https://img.alicdn.com/L1/249/14641382983631/1.0.0/img/14684020340080.png\\\" width=\\\"100%\\\" /></a></td><td style=\\\"width: 1.0%;font-size: 0;\\\">&nbsp;</td><td align=\\\"center\\\" style=\\\"height: 5.0px;\\\" width=\\\"24.25%\\\"><a href=\\\"https://detail.1688.com/offer/578537610605.html\\\" style=\\\"margin-top: -24.0px;display: block;height: 100.0%;width: 36.0px;\\\" target=\\\"_black\\\"><img src=\\\"https://img.alicdn.com/L1/249/14641382983631/1.0.0/img/14684020340080.png\\\" width=\\\"100%\\\" /></a></td></tr><tr><td colspan=\\\"7\\\" style=\\\"font-size: 0;height: 10.0px;\\\">&nbsp;</td></tr></table><div>SHOPTOOL_关联营销_302806_END</div><div>SHOPTOOL_POSITION_TOP_END</div><p><img alt=\\\"3955241823_759960632\\\" height=\\\"885.130890052356\\\" src=\\\"https://cbu01.alicdn.com/img/ibank/2019/523/416/10791614325_759960632.jpg\\\" width=\\\"790\\\" /><br /><br /><img alt=\\\"未标题-1 (2)\\\" src=\\\"https://cbu01.alicdn.com/img/ibank/2019/494/302/11432203494_759960632.jpg\\\" /><br /><br /></p><p><img alt=\\\"undefined\\\" src=\\\"https://cbu01.alicdn.com/img/ibank/2018/103/296/8971692301_759960632.jpg\\\" /><br /><br /><img alt=\\\"undefined\\\" src=\\\"https://cbu01.alicdn.com/img/ibank/2018/338/701/9009107833_759960632.jpg\\\" /><br /><br /><img alt=\\\"undefined\\\" src=\\\"https://cbu01.alicdn.com/img/ibank/2018/199/926/8971629991_759960632.jpg\\\" /><br /><br /><img alt=\\\"undefined\\\" src=\\\"https://cbu01.alicdn.com/img/ibank/2018/522/821/9009128225_759960632.jpg\\\" /><br /><br /><img alt=\\\"undefined\\\" src=\\\"https://cbu01.alicdn.com/img/ibank/2018/131/407/8971704131_759960632.jpg\\\" /><br /><img alt=\\\"undefined\\\" src=\\\"https://cbu01.alicdn.com/img/ibank/2017/165/773/4081377561_759960632.jpg\\\" /><br /><img alt=\\\"undefined\\\" src=\\\"https://cbu01.alicdn.com/img/ibank/2017/628/693/4078396826_759960632.jpg\\\" /><br /><img alt=\\\"undefined\\\" src=\\\"https://cbu01.alicdn.com/img/ibank/2017/195/993/4078399591_759960632.jpg\\\" /></p><p><span style=\\\"font-size: 24.0pt;\\\">新增时尚流行色：豆沙粉、焦糖色</span></p><p><span style=\\\"font-size: 24.0pt;\\\"><img alt=\\\"undefined\\\" src=\\\"https://cbu01.alicdn.com/img/ibank/2019/246/844/11042448642_759960632.jpg\\\" /><img alt=\\\"undefined\\\" src=\\\"https://cbu01.alicdn.com/img/ibank/2019/932/025/11073520239_759960632.jpg\\\" /><br /></span></p><p><img alt=\\\"undefined\\\" src=\\\"https://cbu01.alicdn.com/img/ibank/2017/812/504/4078405218_759960632.jpg\\\" /><br /><img alt=\\\"undefined\\\" src=\\\"https://cbu01.alicdn.com/img/ibank/2017/145/211/4078112541_759960632.jpg\\\" /><br /><img alt=\\\"undefined\\\" src=\\\"https://cbu01.alicdn.com/img/ibank/2017/464/204/4078402464_759960632.jpg\\\" /><br /><img alt=\\\"undefined\\\" src=\\\"https://cbu01.alicdn.com/img/ibank/2017/416/083/4081380614_759960632.jpg\\\" /><br /><img alt=\\\"undefined\\\" src=\\\"https://cbu01.alicdn.com/img/ibank/2017/479/901/4078109974_759960632.jpg\\\" /><br /><img alt=\\\"052下衣_13\\\" src=\\\"https://cbu01.alicdn.com/img/ibank/2018/474/956/9412659474_759960632.jpg\\\" /><br /><br /><br /><br /></p><div id=\\\"offer-template-1452144040852\\\"></div><p><img alt=\\\"002\\\" src=\\\"https://cbu01.alicdn.com/img/ibank/2017/997/137/3961731799_759960632.jpg\\\" /><br /><br /><br /><br /></p><div id=\\\"offer-template-1452144049894\\\"></div><p><img alt=\\\"undefined\\\" src=\\\"https://cbu01.alicdn.com/img/ibank/2018/027/556/8641655720_759960632.jpg\\\" /><br /><br /></p>\",\n" +
            "\t\"item_imgs\": [{\n" +
            "\t\t\"url\": \"https://cbu01.alicdn.com/img/ibank/2017/962/054/4078450269_759960632.jpg\"\n" +
            "\t}, {\n" +
            "\t\t\"url\": \"https://cbu01.alicdn.com/img/ibank/2017/498/393/4078393894_759960632.jpg\"\n" +
            "\t}, {\n" +
            "\t\t\"url\": \"https://cbu01.alicdn.com/img/ibank/2019/507/441/11374144705_759960632.jpg\"\n" +
            "\t}, {\n" +
            "\t\t\"url\": \"https://cbu01.alicdn.com/img/ibank/2017/599/134/4081431995_759960632.jpg\"\n" +
            "\t}, {\n" +
            "\t\t\"url\": \"https://cbu01.alicdn.com/img/ibank/2018/366/713/10157317663_759960632.jpg\"\n" +
            "\t}, {\n" +
            "\t\t\"url\": \"https://cbu01.alicdn.com/img/ibank/2017/048/243/4078342840_759960632.jpg\"\n" +
            "\t}, {\n" +
            "\t\t\"url\": \"https://cbu01.alicdn.com/img/ibank/2017/265/143/4081341562_759960632.jpg\"\n" +
            "\t}, {\n" +
            "\t\t\"url\": \"https://cbu01.alicdn.com/img/ibank/2019/768/957/10511759867_759960632.jpg\"\n" +
            "\t}, {\n" +
            "\t\t\"url\": \"https://cbu01.alicdn.com/img/ibank/2017/364/790/4078097463_759960632.jpg\"\n" +
            "\t}, {\n" +
            "\t\t\"url\": \"https://cbu01.alicdn.com/img/ibank/2017/908/933/4078339809_759960632.jpg\"\n" +
            "\t}, {\n" +
            "\t\t\"url\": \"https://cbu01.alicdn.com/img/ibank/2017/980/460/4078064089_759960632.jpg\"\n" +
            "\t}, {\n" +
            "\t\t\"url\": \"https://cbu01.alicdn.com/img/ibank/2019/266/235/10485532662_759960632.jpg\"\n" +
            "\t}],\n" +
            "\t\"item_weight\": \"0.12 kg\",\n" +
            "\t\"item_size\": \"\",\n" +
            "\t\"location\": \"山东 潍坊\",\n" +
            "\t\"post_fee\": \"\",\n" +
            "\t\"express_fee\": \"\",\n" +
            "\t\"ems_fee\": \"\",\n" +
            "\t\"shipping_to\": \"\",\n" +
            "\t\"has_discount\": \"\",\n" +
            "\t\"video\": [],\n" +
            "\t\"is_virtual\": \"\",\n" +
            "\t\"sample_id\": \"\",\n" +
            "\t\"is_promotion\": \"\",\n" +
            "\t\"props_name\": \"0:0:白色;0:1:黑色;0:2:麻灰;0:3:香芋紫;0:4:酒红;0:5:焦糖色;0:6:豆沙粉;1:0:S;1:1:M;1:2:L;1:3:XL\",\n" +
            "\t\"prop_imgs\": {\n" +
            "\t\t\"prop_img\": [{\n" +
            "\t\t\t\"properties\": \"0:0\",\n" +
            "\t\t\t\"url\": \"https://cbu01.alicdn.com/img/ibank/2017/980/460/4078064089_759960632.jpg\"\n" +
            "\t\t}, {\n" +
            "\t\t\t\"properties\": \"0:1\",\n" +
            "\t\t\t\"url\": \"https://cbu01.alicdn.com/img/ibank/2017/364/790/4078097463_759960632.jpg\"\n" +
            "\t\t}, {\n" +
            "\t\t\t\"properties\": \"0:2\",\n" +
            "\t\t\t\"url\": \"https://cbu01.alicdn.com/img/ibank/2017/908/933/4078339809_759960632.jpg\"\n" +
            "\t\t}, {\n" +
            "\t\t\t\"properties\": \"0:3\",\n" +
            "\t\t\t\"url\": \"https://cbu01.alicdn.com/img/ibank/2017/048/243/4078342840_759960632.jpg\"\n" +
            "\t\t}, {\n" +
            "\t\t\t\"properties\": \"0:4\",\n" +
            "\t\t\t\"url\": \"https://cbu01.alicdn.com/img/ibank/2017/265/143/4081341562_759960632.jpg\"\n" +
            "\t\t}, {\n" +
            "\t\t\t\"properties\": \"0:5\",\n" +
            "\t\t\t\"url\": \"https://cbu01.alicdn.com/img/ibank/2019/768/957/10511759867_759960632.jpg\"\n" +
            "\t\t}, {\n" +
            "\t\t\t\"properties\": \"0:6\",\n" +
            "\t\t\t\"url\": \"https://cbu01.alicdn.com/img/ibank/2019/266/235/10485532662_759960632.jpg\"\n" +
            "\t\t}]\n" +
            "\t},\n" +
            "\t\"property_alias\": \"0:0:白色;0:1:黑色;0:2:麻灰;0:3:香芋紫;0:4:酒红;0:5:焦糖色;0:6:豆沙粉;1:0:S;1:1:M;1:2:L;1:3:XL\",\n" +
            "\t\"props\": [{\n" +
            "\t\t\"name\": \"货号\",\n" +
            "\t\t\"value\": \"020\"\n" +
            "\t}, {\n" +
            "\t\t\"name\": \"产地\",\n" +
            "\t\t\"value\": \"诸城市\"\n" +
            "\t}, {\n" +
            "\t\t\"name\": \"主图来源\",\n" +
            "\t\t\"value\": \"实拍有模特\"\n" +
            "\t}, {\n" +
            "\t\t\"name\": \"货源类别\",\n" +
            "\t\t\"value\": \"现货\"\n" +
            "\t}, {\n" +
            "\t\t\"name\": \"是否库存\",\n" +
            "\t\t\"value\": \"否\"\n" +
            "\t}, {\n" +
            "\t\t\"name\": \"产品类别\",\n" +
            "\t\t\"value\": \"小背心\"\n" +
            "\t}, {\n" +
            "\t\t\"name\": \"风格\",\n" +
            "\t\t\"value\": \"韩版\"\n" +
            "\t}, {\n" +
            "\t\t\"name\": \"款式\",\n" +
            "\t\t\"value\": \"肩带型\"\n" +
            "\t}, {\n" +
            "\t\t\"name\": \"领型\",\n" +
            "\t\t\"value\": \"圆领\"\n" +
            "\t}, {\n" +
            "\t\t\"name\": \"版型\",\n" +
            "\t\t\"value\": \"修身型\"\n" +
            "\t}, {\n" +
            "\t\t\"name\": \"衣长\",\n" +
            "\t\t\"value\": \"短款(40cm＜衣长&le;50cm)\"\n" +
            "\t}, {\n" +
            "\t\t\"name\": \"图案\",\n" +
            "\t\t\"value\": \"纯色\"\n" +
            "\t}, {\n" +
            "\t\t\"name\": \"流行元素\",\n" +
            "\t\t\"value\": \"露背,镂空,露脐\"\n" +
            "\t}, {\n" +
            "\t\t\"name\": \"上市年份/季节\",\n" +
            "\t\t\"value\": \"2017夏季\"\n" +
            "\t}, {\n" +
            "\t\t\"name\": \"面料名称\",\n" +
            "\t\t\"value\": \"莫代尔\"\n" +
            "\t}, {\n" +
            "\t\t\"name\": \"主面料成分\",\n" +
            "\t\t\"value\": \"粘纤\"\n" +
            "\t}, {\n" +
            "\t\t\"name\": \"主面料成分的含量\",\n" +
            "\t\t\"value\": \"91%-99%\"\n" +
            "\t}, {\n" +
            "\t\t\"name\": \"里料成分\",\n" +
            "\t\t\"value\": \"粘纤\"\n" +
            "\t}, {\n" +
            "\t\t\"name\": \"里料成分含量\",\n" +
            "\t\t\"value\": \"91%-99%\"\n" +
            "\t}, {\n" +
            "\t\t\"name\": \"颜色\",\n" +
            "\t\t\"value\": \"白色,黑色,麻灰,香芋紫,酒红,焦糖色,豆沙粉\"\n" +
            "\t}, {\n" +
            "\t\t\"name\": \"尺码\",\n" +
            "\t\t\"value\": \"S,M,L,XL\"\n" +
            "\t}, {\n" +
            "\t\t\"name\": \"是否跨境货源\",\n" +
            "\t\t\"value\": \"否\"\n" +
            "\t}, {\n" +
            "\t\t\"name\": \"适合年龄\",\n" +
            "\t\t\"value\": \"18-24周岁\"\n" +
            "\t}, {\n" +
            "\t\t\"name\": \"面料2成分\",\n" +
            "\t\t\"value\": \"氨纶\"\n" +
            "\t}, {\n" +
            "\t\t\"name\": \"面料2成分的含量\",\n" +
            "\t\t\"value\": \"10%以下\"\n" +
            "\t}],\n" +
            "\t\"total_sold\": \"19\",\n" +
            "\t\"skus\": {\n" +
            "\t\t\"sku\": [{\n" +
            "\t\t\t\"properties\": \"0:0;1:2\",\n" +
            "\t\t\t\"properties_name\": \"0:0:颜色:白色;1:2:尺码:L\",\n" +
            "\t\t\t\"price\": \"18.00\",\n" +
            "\t\t\t\"orginal_price\": \"18.00\",\n" +
            "\t\t\t\"quantity\": 266,\n" +
            "\t\t\t\"spec_id\": \"82114cbd2c10b5e97b01af1510807e2d\",\n" +
            "\t\t\t\"sku_id\": 3494495283628\n" +
            "\t\t}, {\n" +
            "\t\t\t\"properties\": \"0:6;1:0\",\n" +
            "\t\t\t\"properties_name\": \"0:6:颜色:豆沙粉;1:0:尺码:S\",\n" +
            "\t\t\t\"price\": \"18.00\",\n" +
            "\t\t\t\"orginal_price\": \"18.00\",\n" +
            "\t\t\t\"quantity\": 500,\n" +
            "\t\t\t\"spec_id\": \"e24e2168ce55292bc3504d2f3d669d1d\",\n" +
            "\t\t\t\"sku_id\": 4177573987642\n" +
            "\t\t}, {\n" +
            "\t\t\t\"properties\": \"0:5;1:3\",\n" +
            "\t\t\t\"properties_name\": \"0:5:颜色:焦糖色;1:3:尺码:XL\",\n" +
            "\t\t\t\"price\": \"18.00\",\n" +
            "\t\t\t\"orginal_price\": \"18.00\",\n" +
            "\t\t\t\"quantity\": 500,\n" +
            "\t\t\t\"spec_id\": \"5dbee49739cbb8c0754394e08cfd8258\",\n" +
            "\t\t\t\"sku_id\": 4177573987647\n" +
            "\t\t}, {\n" +
            "\t\t\t\"properties\": \"0:3;1:3\",\n" +
            "\t\t\t\"properties_name\": \"0:3:颜色:香芋紫;1:3:尺码:XL\",\n" +
            "\t\t\t\"price\": \"18.00\",\n" +
            "\t\t\t\"orginal_price\": \"18.00\",\n" +
            "\t\t\t\"quantity\": 0,\n" +
            "\t\t\t\"spec_id\": \"7d3b22215135b653fba91e664feb3f35\",\n" +
            "\t\t\t\"sku_id\": 3494495283644\n" +
            "\t\t}, {\n" +
            "\t\t\t\"properties\": \"0:4;1:2\",\n" +
            "\t\t\t\"properties_name\": \"0:4:颜色:酒红;1:2:尺码:L\",\n" +
            "\t\t\t\"price\": \"18.00\",\n" +
            "\t\t\t\"orginal_price\": \"18.00\",\n" +
            "\t\t\t\"quantity\": 0,\n" +
            "\t\t\t\"spec_id\": \"2237ea018b87555e553411a220ad71d2\",\n" +
            "\t\t\t\"sku_id\": 3494495283648\n" +
            "\t\t}, {\n" +
            "\t\t\t\"properties\": \"0:4;1:3\",\n" +
            "\t\t\t\"properties_name\": \"0:4:颜色:酒红;1:3:尺码:XL\",\n" +
            "\t\t\t\"price\": \"18.00\",\n" +
            "\t\t\t\"orginal_price\": \"18.00\",\n" +
            "\t\t\t\"quantity\": 0,\n" +
            "\t\t\t\"spec_id\": \"ea2a2a5af809525ec10b9991c1801023\",\n" +
            "\t\t\t\"sku_id\": 3494495283649\n" +
            "\t\t}, {\n" +
            "\t\t\t\"properties\": \"0:4;1:1\",\n" +
            "\t\t\t\"properties_name\": \"0:4:颜色:酒红;1:1:尺码:M\",\n" +
            "\t\t\t\"price\": \"18.00\",\n" +
            "\t\t\t\"orginal_price\": \"18.00\",\n" +
            "\t\t\t\"quantity\": 0,\n" +
            "\t\t\t\"spec_id\": \"2a81b80b886f7ba1fcccb9ee851b37a1\",\n" +
            "\t\t\t\"sku_id\": 3494495283647\n" +
            "\t\t}, {\n" +
            "\t\t\t\"properties\": \"0:0;1:0\",\n" +
            "\t\t\t\"properties_name\": \"0:0:颜色:白色;1:0:尺码:S\",\n" +
            "\t\t\t\"price\": \"18.00\",\n" +
            "\t\t\t\"orginal_price\": \"18.00\",\n" +
            "\t\t\t\"quantity\": 296,\n" +
            "\t\t\t\"spec_id\": \"94d1d179497744028aa76873afdeba62\",\n" +
            "\t\t\t\"sku_id\": 3494495283626\n" +
            "\t\t}, {\n" +
            "\t\t\t\"properties\": \"0:6;1:2\",\n" +
            "\t\t\t\"properties_name\": \"0:6:颜色:豆沙粉;1:2:尺码:L\",\n" +
            "\t\t\t\"price\": \"18.00\",\n" +
            "\t\t\t\"orginal_price\": \"18.00\",\n" +
            "\t\t\t\"quantity\": 499,\n" +
            "\t\t\t\"spec_id\": \"2c495d3fbf256597b36e122a55997119\",\n" +
            "\t\t\t\"sku_id\": 4177573987646\n" +
            "\t\t}, {\n" +
            "\t\t\t\"properties\": \"0:2;1:3\",\n" +
            "\t\t\t\"properties_name\": \"0:2:颜色:麻灰;1:3:尺码:XL\",\n" +
            "\t\t\t\"price\": \"18.00\",\n" +
            "\t\t\t\"orginal_price\": \"18.00\",\n" +
            "\t\t\t\"quantity\": 500,\n" +
            "\t\t\t\"spec_id\": \"87df69d24597f71ef0b428d7230c9cbf\",\n" +
            "\t\t\t\"sku_id\": 3494495283639\n" +
            "\t\t}, {\n" +
            "\t\t\t\"properties\": \"0:0;1:1\",\n" +
            "\t\t\t\"properties_name\": \"0:0:颜色:白色;1:1:尺码:M\",\n" +
            "\t\t\t\"price\": \"18.00\",\n" +
            "\t\t\t\"orginal_price\": \"18.00\",\n" +
            "\t\t\t\"quantity\": 248,\n" +
            "\t\t\t\"spec_id\": \"cef93beef156f1799e736c649f36efae\",\n" +
            "\t\t\t\"sku_id\": 3494495283627\n" +
            "\t\t}, {\n" +
            "\t\t\t\"properties\": \"0:6;1:3\",\n" +
            "\t\t\t\"properties_name\": \"0:6:颜色:豆沙粉;1:3:尺码:XL\",\n" +
            "\t\t\t\"price\": \"18.00\",\n" +
            "\t\t\t\"orginal_price\": \"18.00\",\n" +
            "\t\t\t\"quantity\": 500,\n" +
            "\t\t\t\"spec_id\": \"955f76d58ab447242c93a421c1d3db00\",\n" +
            "\t\t\t\"sku_id\": 4177573987648\n" +
            "\t\t}, {\n" +
            "\t\t\t\"properties\": \"0:6;1:1\",\n" +
            "\t\t\t\"properties_name\": \"0:6:颜色:豆沙粉;1:1:尺码:M\",\n" +
            "\t\t\t\"price\": \"18.00\",\n" +
            "\t\t\t\"orginal_price\": \"18.00\",\n" +
            "\t\t\t\"quantity\": 500,\n" +
            "\t\t\t\"spec_id\": \"938c48f2cfd2420d462eb5bdda866cf5\",\n" +
            "\t\t\t\"sku_id\": 4177573987644\n" +
            "\t\t}, {\n" +
            "\t\t\t\"properties\": \"0:4;1:0\",\n" +
            "\t\t\t\"properties_name\": \"0:4:颜色:酒红;1:0:尺码:S\",\n" +
            "\t\t\t\"price\": \"18.00\",\n" +
            "\t\t\t\"orginal_price\": \"18.00\",\n" +
            "\t\t\t\"quantity\": 0,\n" +
            "\t\t\t\"spec_id\": \"db2008400b4ecdf3779698df22c5191f\",\n" +
            "\t\t\t\"sku_id\": 3494495283646\n" +
            "\t\t}, {\n" +
            "\t\t\t\"properties\": \"0:3;1:1\",\n" +
            "\t\t\t\"properties_name\": \"0:3:颜色:香芋紫;1:1:尺码:M\",\n" +
            "\t\t\t\"price\": \"18.00\",\n" +
            "\t\t\t\"orginal_price\": \"18.00\",\n" +
            "\t\t\t\"quantity\": 0,\n" +
            "\t\t\t\"spec_id\": \"b44651d0a0c17d9b5898e2dbde18aeea\",\n" +
            "\t\t\t\"sku_id\": 3494495283642\n" +
            "\t\t}, {\n" +
            "\t\t\t\"properties\": \"0:1;1:3\",\n" +
            "\t\t\t\"properties_name\": \"0:1:颜色:黑色;1:3:尺码:XL\",\n" +
            "\t\t\t\"price\": \"18.00\",\n" +
            "\t\t\t\"orginal_price\": \"18.00\",\n" +
            "\t\t\t\"quantity\": 500,\n" +
            "\t\t\t\"spec_id\": \"5c337082186ff55b2d8267560ac89d59\",\n" +
            "\t\t\t\"sku_id\": 3494495283634\n" +
            "\t\t}, {\n" +
            "\t\t\t\"properties\": \"0:2;1:2\",\n" +
            "\t\t\t\"properties_name\": \"0:2:颜色:麻灰;1:2:尺码:L\",\n" +
            "\t\t\t\"price\": \"18.00\",\n" +
            "\t\t\t\"orginal_price\": \"18.00\",\n" +
            "\t\t\t\"quantity\": 498,\n" +
            "\t\t\t\"spec_id\": \"adcf95facac9f670abf4c74eab90ac86\",\n" +
            "\t\t\t\"sku_id\": 3494495283638\n" +
            "\t\t}, {\n" +
            "\t\t\t\"properties\": \"0:3;1:2\",\n" +
            "\t\t\t\"properties_name\": \"0:3:颜色:香芋紫;1:2:尺码:L\",\n" +
            "\t\t\t\"price\": \"18.00\",\n" +
            "\t\t\t\"orginal_price\": \"18.00\",\n" +
            "\t\t\t\"quantity\": 0,\n" +
            "\t\t\t\"spec_id\": \"1d1fc72d6a1787914c626b2236b943ca\",\n" +
            "\t\t\t\"sku_id\": 3494495283643\n" +
            "\t\t}, {\n" +
            "\t\t\t\"properties\": \"0:0;1:3\",\n" +
            "\t\t\t\"properties_name\": \"0:0:颜色:白色;1:3:尺码:XL\",\n" +
            "\t\t\t\"price\": \"18.00\",\n" +
            "\t\t\t\"orginal_price\": \"18.00\",\n" +
            "\t\t\t\"quantity\": 400,\n" +
            "\t\t\t\"spec_id\": \"c45d8408137e34adf8e695250c42a2e9\",\n" +
            "\t\t\t\"sku_id\": 3494495283629\n" +
            "\t\t}, {\n" +
            "\t\t\t\"properties\": \"0:1;1:0\",\n" +
            "\t\t\t\"properties_name\": \"0:1:颜色:黑色;1:0:尺码:S\",\n" +
            "\t\t\t\"price\": \"18.00\",\n" +
            "\t\t\t\"orginal_price\": \"18.00\",\n" +
            "\t\t\t\"quantity\": 198,\n" +
            "\t\t\t\"spec_id\": \"eb81c61de14f4adb405ffcc2c8a4a3fb\",\n" +
            "\t\t\t\"sku_id\": 3494495283631\n" +
            "\t\t}, {\n" +
            "\t\t\t\"properties\": \"0:5;1:1\",\n" +
            "\t\t\t\"properties_name\": \"0:5:颜色:焦糖色;1:1:尺码:M\",\n" +
            "\t\t\t\"price\": \"18.00\",\n" +
            "\t\t\t\"orginal_price\": \"18.00\",\n" +
            "\t\t\t\"quantity\": 500,\n" +
            "\t\t\t\"spec_id\": \"c4e631ae720e6f12d03ed364c138b577\",\n" +
            "\t\t\t\"sku_id\": 4177573987643\n" +
            "\t\t}, {\n" +
            "\t\t\t\"properties\": \"0:5;1:2\",\n" +
            "\t\t\t\"properties_name\": \"0:5:颜色:焦糖色;1:2:尺码:L\",\n" +
            "\t\t\t\"price\": \"18.00\",\n" +
            "\t\t\t\"orginal_price\": \"18.00\",\n" +
            "\t\t\t\"quantity\": 500,\n" +
            "\t\t\t\"spec_id\": \"ec1c9edd263dccf1cb8d1fa06ed366af\",\n" +
            "\t\t\t\"sku_id\": 4177573987645\n" +
            "\t\t}, {\n" +
            "\t\t\t\"properties\": \"0:1;1:2\",\n" +
            "\t\t\t\"properties_name\": \"0:1:颜色:黑色;1:2:尺码:L\",\n" +
            "\t\t\t\"price\": \"18.00\",\n" +
            "\t\t\t\"orginal_price\": \"18.00\",\n" +
            "\t\t\t\"quantity\": 498,\n" +
            "\t\t\t\"spec_id\": \"b99ece08861a79e13a4dba90e97ebff8\",\n" +
            "\t\t\t\"sku_id\": 3494495283633\n" +
            "\t\t}, {\n" +
            "\t\t\t\"properties\": \"0:2;1:1\",\n" +
            "\t\t\t\"properties_name\": \"0:2:颜色:麻灰;1:1:尺码:M\",\n" +
            "\t\t\t\"price\": \"18.00\",\n" +
            "\t\t\t\"orginal_price\": \"18.00\",\n" +
            "\t\t\t\"quantity\": 498,\n" +
            "\t\t\t\"spec_id\": \"c6c6f8f0a6afa4dffbcf1b23ef2dc0c8\",\n" +
            "\t\t\t\"sku_id\": 3494495283637\n" +
            "\t\t}, {\n" +
            "\t\t\t\"properties\": \"0:5;1:0\",\n" +
            "\t\t\t\"properties_name\": \"0:5:颜色:焦糖色;1:0:尺码:S\",\n" +
            "\t\t\t\"price\": \"18.00\",\n" +
            "\t\t\t\"orginal_price\": \"18.00\",\n" +
            "\t\t\t\"quantity\": 500,\n" +
            "\t\t\t\"spec_id\": \"b5de9b71054c7ec68d08dc413213a84b\",\n" +
            "\t\t\t\"sku_id\": 4177573987641\n" +
            "\t\t}, {\n" +
            "\t\t\t\"properties\": \"0:1;1:1\",\n" +
            "\t\t\t\"properties_name\": \"0:1:颜色:黑色;1:1:尺码:M\",\n" +
            "\t\t\t\"price\": \"18.00\",\n" +
            "\t\t\t\"orginal_price\": \"18.00\",\n" +
            "\t\t\t\"quantity\": 293,\n" +
            "\t\t\t\"spec_id\": \"4b2120e532948daa11e58e09bb260801\",\n" +
            "\t\t\t\"sku_id\": 3494495283632\n" +
            "\t\t}, {\n" +
            "\t\t\t\"properties\": \"0:2;1:0\",\n" +
            "\t\t\t\"properties_name\": \"0:2:颜色:麻灰;1:0:尺码:S\",\n" +
            "\t\t\t\"price\": \"18.00\",\n" +
            "\t\t\t\"orginal_price\": \"18.00\",\n" +
            "\t\t\t\"quantity\": 499,\n" +
            "\t\t\t\"spec_id\": \"8a9dacf6946c25843d1af8a0a8a56eaf\",\n" +
            "\t\t\t\"sku_id\": 3494495283636\n" +
            "\t\t}, {\n" +
            "\t\t\t\"properties\": \"0:3;1:0\",\n" +
            "\t\t\t\"properties_name\": \"0:3:颜色:香芋紫;1:0:尺码:S\",\n" +
            "\t\t\t\"price\": \"18.00\",\n" +
            "\t\t\t\"orginal_price\": \"18.00\",\n" +
            "\t\t\t\"quantity\": 500,\n" +
            "\t\t\t\"spec_id\": \"b47be83bef03ad456c9544acd0d9966c\",\n" +
            "\t\t\t\"sku_id\": 3494495283641\n" +
            "\t\t}]\n" +
            "\t},\n" +
            "\t\"seller_id\": \"2701309825\",\n" +
            "\t\"sales\": \"19\",\n" +
            "\t\"shop_id\": \"\",\n" +
            "\t\"props_list\": {\n" +
            "\t\t\"0:0\": \"颜色:白色\",\n" +
            "\t\t\"0:1\": \"颜色:黑色\",\n" +
            "\t\t\"0:2\": \"颜色:麻灰\",\n" +
            "\t\t\"0:3\": \"颜色:香芋紫\",\n" +
            "\t\t\"0:4\": \"颜色:酒红\",\n" +
            "\t\t\"0:5\": \"颜色:焦糖色\",\n" +
            "\t\t\"0:6\": \"颜色:豆沙粉\",\n" +
            "\t\t\"1:0\": \"尺码:S\",\n" +
            "\t\t\"1:1\": \"尺码:M\",\n" +
            "\t\t\"1:2\": \"尺码:L\",\n" +
            "\t\t\"1:3\": \"尺码:XL\"\n" +
            "\t},\n" +
            "\t\"seller_info\": {\n" +
            "\t\t\"level\": \"\",\n" +
            "\t\t\"shop_type\": \"A\",\n" +
            "\t\t\"user_num_id\": \"2701309825\",\n" +
            "\t\t\"cid\": null,\n" +
            "\t\t\"delivery_score\": null,\n" +
            "\t\t\"item_score\": null,\n" +
            "\t\t\"score_p\": null,\n" +
            "\t\t\"city\": \"山东 潍坊\",\n" +
            "\t\t\"nick\": \"迈捷制衣\",\n" +
            "\t\t\"sid\": \"b2b-270130982551d77\",\n" +
            "\t\t\"company_name\": \"诸城迈捷服饰源头厂家\",\n" +
            "\t\t\"title\": \"诸城迈捷服饰源头厂家\",\n" +
            "\t\t\"shop_name\": \"诸城迈捷服饰源头厂家\",\n" +
            "\t\t\"zhuy\": \"https://zcmjfs.1688.com\"\n" +
            "\t},\n" +
            "\t\"tmall\": \"false\",\n" +
            "\t\"error\": \"\",\n" +
            "\t\"warning\": \"\",\n" +
            "\t\"url_log\": [],\n" +
            "\t\"is_support_mix\": \"true\",\n" +
            "\t\"mix_amount\": \"60\",\n" +
            "\t\"mix_begin\": \"\",\n" +
            "\t\"mix_number\": \"5\",\n" +
            "\t\"priceRange\": [\n" +
            "\t\t[5, 18],\n" +
            "\t\t[50, 17]\n" +
            "\t],\n" +
            "\t\"priceRangeOriginal\": [\n" +
            "\t\t[5, 18],\n" +
            "\t\t[50, 17]\n" +
            "\t],\n" +
            "\t\"m_weight\": \"0.12 kg\",\n" +
            "\t\"j_weight\": \"0.12 kg\",\n" +
            "\t\"volume\": \"\",\n" +
            "\t\"data_form\": \"1688p\",\n" +
            "\t\"props_img\": {\n" +
            "\t\t\"0:0\": \"https://cbu01.alicdn.com/img/ibank/2017/980/460/4078064089_759960632.jpg\",\n" +
            "\t\t\"0:1\": \"https://cbu01.alicdn.com/img/ibank/2017/364/790/4078097463_759960632.jpg\",\n" +
            "\t\t\"0:2\": \"https://cbu01.alicdn.com/img/ibank/2017/908/933/4078339809_759960632.jpg\",\n" +
            "\t\t\"0:3\": \"https://cbu01.alicdn.com/img/ibank/2017/048/243/4078342840_759960632.jpg\",\n" +
            "\t\t\"0:4\": \"https://cbu01.alicdn.com/img/ibank/2017/265/143/4081341562_759960632.jpg\",\n" +
            "\t\t\"0:5\": \"https://cbu01.alicdn.com/img/ibank/2019/768/957/10511759867_759960632.jpg\",\n" +
            "\t\t\"0:6\": \"https://cbu01.alicdn.com/img/ibank/2019/266/235/10485532662_759960632.jpg\"\n" +
            "\t},\n" +
            "\t\"shop_item\": [],\n" +
            "\t\"relate_items\": []\n" +
            "}";

    private static void accept(Map.Entry<String, String> entry) {
        System.out.println(entry.getKey() + " " + entry.getValue());
    }
}
