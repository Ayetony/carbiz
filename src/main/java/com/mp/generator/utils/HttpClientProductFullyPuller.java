package com.mp.generator.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HttpClientProductFullyPuller {

    public static String getJsonByGetRequest(String id, boolean nocache) {

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        String oneBoundApi = "https://api-gw.onebound.cn/1688/item_get/?key=tel18606528273&secret=20200417&num_iid=";

        HttpPost httpPost ;
        if(nocache){
            httpPost = new HttpPost(oneBoundApi + id + "&cache=no" );//"&cache=no"
        }else {
            httpPost = new HttpPost(oneBoundApi + id);
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
        String str = HttpClientProductFullyPuller.getJsonByGetRequest("610947572360",true);
        System.out.println(str);
    }

    public static String purify(String json){
        if(json == null){
            return null;
        }
        JsonParser parser;
        parser = new JsonParser();
        JsonObject jsonObject = parser.parse(json).getAsJsonObject();
        if(jsonObject.isJsonObject()){
            return jsonObject.get("item").toString();
        }
        return null;
    }

}
