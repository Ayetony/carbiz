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
        // shop_ref http://sinoks2008.1688.com/
//        shopRef = StringUtils.substring(shopRef,0,shopRef.length() - 1);
        String oneBoundApi ="https://api-gw.onebound.cn/1688/seller_info/?key=tel18606528273&nick=&shop_url="+ shopRef + "&cache=no&&lang=zh-CN&secret=20200417";
        HttpPost httpPost ;
        httpPost = new HttpPost(oneBoundApi);
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

        System.out.println(getJson("https://yylsmould.1688.com"));  //https://0572xcy.1688.com http://sinoks2008.1688.com/

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

}
