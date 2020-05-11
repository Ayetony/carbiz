package com.mp.generator.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mp.generator.entity.AlibabaSupplierInfoPo;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class HttpClientSupplierPuller {

    public static JsonElement getJson(String shopRef) {

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(8000).setConnectTimeout(8000).build();
        // shop_ref http://sinoks2008.1688.com/
        String oneBoundApi = "https://api.onebound.cn/1688/api_call.php?nick=&shop_url=" + shopRef + "&api_name=seller_info&lang=zh-CN&key=tel18606528273&secret=20200417";
        HttpPost httpPost ;
        httpPost = new HttpPost(oneBoundApi);
        httpPost.setConfig(requestConfig);
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
        return purifyUser(responseStr);
    }

    public static void main(String[] args) {

        System.out.println(supplierPoFromJson("https://0572xcy.1688.com"));  //https://0572xcy.1688.com http://sinoks2008.1688.com/

    }

    private static JsonElement  purifyUser(String json){
        if(json == null){
            return null;
        }
        JsonParser parser;
        parser = new JsonParser();
        JsonObject jsonObject = parser.parse(json).getAsJsonObject();
        if(jsonObject.isJsonObject()){
            return jsonObject.get("user");
        }
        return null;
    }



    public static AlibabaSupplierInfoPo supplierPoFromJson(String shopRef){

        AlibabaSupplierInfoPo alibabaSupplierInfoPo = new AlibabaSupplierInfoPo();
       JsonElement element =  getJson(shopRef);
       if(element == null){
           System.out.println("missing content : shop Link" + shopRef);
           return null;
       }
       JsonObject jsonObject = element.getAsJsonObject();
        System.out.println(element.getAsJsonObject().toString());
       String biz_type_model = jsonObject.get("biz_type_model").getAsString();
       String  creditSellerRank = jsonObject.get("sale_level").getAsString();
       String shopLocation = jsonObject.get("address").getAsString();
       String companyName = jsonObject.get("title").getAsString();

       JsonObject base_info  = jsonObject.get("base_info").getAsJsonObject();
       JsonObject buyer_service = jsonObject.get("buyer_service").getAsJsonObject();
       if(StringUtils.equals(jsonObject.get("trade_info").toString(),"[]")){
           return null;
       }
       JsonObject trade_info = jsonObject.get("trade_info").getAsJsonObject();

       //base info
       String start_time = base_info.get("start_time").getAsString();
//       String creditSellerRank = base_info.get("credit_seller_rank").getAsString();

       //buyer service
        String productDescribeCompareWithAverageRate = buyer_service.get("product_describe_compare_with_average_rate").getAsString();
        String replyTimeCompareWithAverageRate = buyer_service.get("reply_time_compare_with_average_rate").getAsString();
        String deliverTimeCompareWithAverageRate = buyer_service.get("delivery_time_compare_with_average_rate").getAsString();
        String returnRate = buyer_service.get("return_rate").getAsString();

        //trade info
        String numberCummulativeSale = trade_info.get("number_cummulative_sale").getAsString();
        String numberOfBuyer = trade_info.get("number_of_buyer").getAsString();
        String customerRebuyRate = trade_info.get("customer_rebuy_rate").getAsString();
        String disputeRate = trade_info.get("dispute_rate").getAsString();


        alibabaSupplierInfoPo.setBusinessType(biz_type_model);
        alibabaSupplierInfoPo.setShopRef(shopRef);
        alibabaSupplierInfoPo.setStartTime(start_time);
        alibabaSupplierInfoPo.setShopName(companyName);
        alibabaSupplierInfoPo.setShopLocation(shopLocation);
        alibabaSupplierInfoPo.setCreditSellerRank(creditSellerRank);
        alibabaSupplierInfoPo.setCustomerRebuyRate(customerRebuyRate);
        alibabaSupplierInfoPo.setDeliverTimeCompareWithAverageRate(deliverTimeCompareWithAverageRate);
        alibabaSupplierInfoPo.setDisputeRate(disputeRate);
        alibabaSupplierInfoPo.setNumberCummulativeSale(numberCummulativeSale);
        alibabaSupplierInfoPo.setNumberOfBuyer(numberOfBuyer);
        alibabaSupplierInfoPo.setReplyTimeCompareWithAverageRate(replyTimeCompareWithAverageRate);
        alibabaSupplierInfoPo.setReturnRate(returnRate);
        alibabaSupplierInfoPo.setProductDescribeCompareWithAverageRate(productDescribeCompareWithAverageRate);
        alibabaSupplierInfoPo.setCrawlTime(LocalDateTime.now().toString());

        return alibabaSupplierInfoPo;

    }





}
