package com.mp.generator.utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mp.generator.entity.AlibabaProductInfoPo;
import com.mp.generator.mapper.AlibabaProductInfoPoMapper;
import com.mp.generator.service.IAlibabaProductInfoPoService;
import com.mp.generator.service.impl.AlibabaProductInfoPoServiceImpl;
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
import java.util.concurrent.atomic.AtomicInteger;

public class HttpClientPuller {

    private AtomicInteger count = new AtomicInteger();

    public static JsonElement getJsonByGetRequest(String id) {

         CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        String oneBoundApi = "https://api.onebound.cn/1688/api_call.php?key=tel18606528273&secret=20200417&api_name=item_get&num_iid=";
        HttpPost httpPost = new HttpPost(oneBoundApi + id);//"&cache=no"
         // 响应模型
         CloseableHttpResponse response = null;
         String responseStr = "";
         try {
             // 由客户端执行(发送请求
             response = httpClient.execute(httpPost);
             // 从响应模型中获取响应实体
             HttpEntity responseEntity = response.getEntity();
             System.out.println("响应状态为:" + response.getStatusLine());
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
        new HttpClientPuller().productInfoFromJson("533816674053");//533816674053 614252193570
    }


    public String  productInfoFromJson(String id) {

        JsonElement element = getJsonByGetRequest(id);
        if(element == null){
            System.out.println("空的错误");
            element = getJsonByGetRequest(id);
            if(element == null) {
                return "error --------" + id;
            }
        }
        AlibabaProductInfoPo productInfoPo = new AlibabaProductInfoPo();
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
        String brand = element.getAsJsonObject().get("brand").toString();
        String shop_id = element.getAsJsonObject().get("shop_id").toString();

        String currentPrice;
        if(!priceRange.equals("null") && StringUtils.isNotBlank(priceRange.replaceAll("\\[]",""))){
            currentPrice = priceRange;
        }else{
            currentPrice = "["+ min_num + "," + basic_price + "]";
        }

        String  productLink = element.getAsJsonObject().get("detail_url").getAsString();
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
        productInfoPo.setBrand(brand);
        productInfoPo.setShopID(shop_id);


        Multimap<String,String> skus = ArrayListMultimap.create();
        if(StringUtils.isBlank(element.getAsJsonObject().get("props_img").toString().replaceAll("\\[]" ,""))){
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
                String size = sku[1];
                String imgURL = imgPropMap.get(color);
                String price = "price:" + e.getAsJsonObject().get("price").getAsString();
                String quantity = "quantity:" + e.getAsJsonObject().get("quantity").getAsString();
                skus.put(trimColons(size) + ";"+ color + ";" + price + ";" + quantity,imgURL);
            } );
        }
        skus.entries().forEach(HttpClientPuller::accept);
        intoBase(skus,productInfoPo);
        return "ok";
    }

    public void intoBase(Multimap<String, String> skus, AlibabaProductInfoPo alibabaProductInfoPo){


        for (Map.Entry<String, String> entry : skus.entries()) {
            alibabaProductInfoPo.setSku(entry.getValue());
            alibabaProductInfoPo.setSizePriceStock(entry.getKey());
            alibabaProductInfoPo.setSourceSite("1688.com");
            AlibabaProductInfoPoServiceImpl service = new AlibabaProductInfoPoServiceImpl();
            System.out.println(alibabaProductInfoPo.toString());
            service.getBaseMapper().insert(alibabaProductInfoPo);
            count.incrementAndGet();
            System.out.println("正式入库：count" + count);
        }


    }



    public static String trimColons(String size){
        int index = StringUtils.ordinalIndexOf(size, ":", 2);//冒号多次
        return StringUtils.substring(size,index+1);
    }

    private static JsonElement  purify(String json){
        JsonParser parser = new JsonParser();
        JsonElement element =  parser.parse(json).getAsJsonObject().get("item").getAsJsonObject();
        if(element.isJsonNull()){
            return null;
        }
        return  element;
    }


    private static Map<String,String> mapJson(JsonElement element, String key){

        Map<String,String> hashMap = new HashMap<>();
        element.getAsJsonObject().get(key).getAsJsonObject().entrySet().forEach( e -> hashMap.put(e.getKey().replace("\"",""),
                e.getValue().toString().replace("\"","")));
        return hashMap;

    }

    private static void accept(Map.Entry<String, String> entry) {
        System.out.println(entry.getKey() + " " + entry.getValue());
    }
}
