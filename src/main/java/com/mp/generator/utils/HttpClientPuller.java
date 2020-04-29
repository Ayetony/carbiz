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


    public static JsonElement getJsonByGetRequest(String id,boolean nocache) {

         CloseableHttpClient httpClient = HttpClientBuilder.create().build();
         String oneBoundApi = "https://api.onebound.cn/1688/api_call.php?key=tel18606528273&secret=20200417&api_name=item_get&num_iid=";

         HttpPost httpPost ;
        if(nocache){
            httpPost = new HttpPost(oneBoundApi + id);//"&cache=no"
        }else {
            httpPost = new HttpPost(oneBoundApi + "&cache=no" + id);
        }
         // 响应模型
         CloseableHttpResponse response = null;
         String responseStr = "";
         try {
             // 由客户端执行(发送请求
             response = httpClient.execute(httpPost);
             // 从响应模型中获取响应实体
             HttpEntity responseEntity = response.getEntity();
             String status = response.getStatusLine().toString();
             System.out.println("响应状态为:" + status);
             if(!status.contains("200")){
                 return null;
             }

             if (responseEntity != null) {
                 responseStr = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
             }
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
        //产品详情解析
        new HttpClientPuller().productInfoFromJson("1136562860");//533816674053 614252193570

    }


    public Map<AlibabaProductInfoPo,Multimap<String,String>>  productInfoFromJson(String id) {

        JsonElement element = getJsonByGetRequest(id,false);
        if( element == null || !element.isJsonObject()){
            System.out.println("missing content item :" + element + id);
            return null;
        }
        //props_list 不能为空
        JsonElement elementFlag = element.getAsJsonObject().get("props_list");
        if(elementFlag==null || !elementFlag.isJsonObject()){
            System.out.println("missing content props list : " + elementFlag + id );
            return null;
        }


        Map<String, String> propListMap = mapJson(element, "props_list");
        Map<String, String> imgPropMap = new HashMap<>();
        String crossBorderPro = "";
        if(element.getAsJsonObject().get("m_weight") != null)
           crossBorderPro += "packageWeight:" + element.getAsJsonObject().get("m_weight").getAsString() + ";";

        if(element.getAsJsonObject().get("j_weight") != null)
           crossBorderPro += "unitWeight:" + element.getAsJsonObject().get("j_weight").getAsString() + ";";

        if(element.getAsJsonObject().get("volume") != null)
            crossBorderPro += "volume:" + element.getAsJsonObject().get("volume").getAsString() + ";";

        String productId = element.getAsJsonObject().get("num_iid").getAsString();
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
        String expree_fee = element.getAsJsonObject().get("express_fee").getAsString();
        String props = element.getAsJsonObject().get("props").toString();
        String brand = element.getAsJsonObject().get("brand").getAsString();
        String shop_id = element.getAsJsonObject().get("shop_id").getAsString();


        String currentPrice;
        if(StringUtils.isNotBlank(priceRange.replace("[]","")) && !priceRange.equals("null")){
            currentPrice = priceRange;
        }else{
            currentPrice = "["+ min_num + "," + basic_price + "]";
        }

        String  productLink = element.getAsJsonObject().get("detail_url").getAsString();



        Multimap<String,String> skus = ArrayListMultimap.create();
        JsonElement img = element.getAsJsonObject().get("props_img");
        // Judge the existence
        if( img == null || !img.isJsonObject()){
            element.getAsJsonObject().get("skus").getAsJsonObject().get("sku").
                    getAsJsonArray().forEach( e -> {
                String name = e.getAsJsonObject().get("properties_name").getAsString();
                String color = StringUtils.substring(name,name.lastIndexOf("颜色:"))+";";
                String price = "price:" + e.getAsJsonObject().get("price").getAsString()+";";
                String quantity = "quantity:" + e.getAsJsonObject().get("quantity").getAsString();
                skus.put(color + price + quantity,null);
            } );

        }else {
            Map<String, String> propImgMap = mapJson(element, "props_img");
            propImgMap.forEach((key, value) ->{ if(propListMap.get(key)!=null) imgPropMap.put(propListMap.get(key), value);});
            element.getAsJsonObject().get("skus").getAsJsonObject().get("sku").
                    getAsJsonArray().forEach( e -> {
                String name = e.getAsJsonObject().get("properties_name").getAsString();
                String[] sku = name.split(";");
                String color = StringUtils.substring(sku[0],sku[0].lastIndexOf("颜色:"));
                String size = "" ;
                if(sku.length>1) {
                    size = sku[1];
                }
                String imgURL = imgPropMap.get(color);
                String price = "price:" + e.getAsJsonObject().get("price").getAsString();
                String quantity = "quantity:" + e.getAsJsonObject().get("quantity").getAsString();
                skus.put(trimColons(size) + ";"+ color + ";" + price + ";" + quantity,imgURL);
            } );
        }
        skus.entries().forEach(HttpClientPuller::accept);

        AlibabaProductInfoPo productInfoPo = new AlibabaProductInfoPo();
        // 入库产品
        productInfoPo.setProductRef(productLink);
        productInfoPo.setCrossBorderPro(crossBorderPro);
        productInfoPo.setProductImgLink(productImgLink);
        productInfoPo.setCurrentPrice(currentPrice);
        productInfoPo.setShopRef(shopRef);
        productInfoPo.setDeliveryAddress(deliveryAddress);
        productInfoPo.setTotalSaleThisMonth(totalSaleThisMonth);
        productInfoPo.setShopName(shopName);
        productInfoPo.setProductName(productName);
        productInfoPo.setFastShippingFee(expree_fee);
        productInfoPo.setProductDetail(props);
        productInfoPo.setProductIDInSourceSite(productId);
        productInfoPo.setBrand(brand.replace("\"",""));
        productInfoPo.setShopID(shop_id);

        Map<AlibabaProductInfoPo,Multimap<String,String>> hashMap = new HashMap<>();
        hashMap.put(productInfoPo, skus);
        return hashMap;
    }



    public static String trimColons(String size){
        int index = StringUtils.ordinalIndexOf(size, ":", 2);//冒号多次
        return StringUtils.substring(size,index+1);
    }

    private static JsonElement  purify(String json){
        JsonParser parser;
        parser = new JsonParser();
        if(json == null){
            return null;
        }
        return parser.parse(json).getAsJsonObject().get("item");
    }


    private static Map<String,String> mapJson(JsonElement element, String key){

        Map<String,String> hashMap = new HashMap<>();
        try {
            element.getAsJsonObject().get(key).getAsJsonObject().entrySet().forEach(e -> hashMap.put(e.getKey().replace("\"", ""),
                    e.getValue().toString().replace("\"", "")));
        }catch (IllegalStateException e){
            throw new RuntimeException("element:" + element);
        }
        return hashMap;

    }

    private static void accept(Map.Entry<String, String> entry) {
        System.out.println(entry.getKey() + " " + entry.getValue());
    }
}
