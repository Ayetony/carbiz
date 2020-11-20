package com.mp.generator.utils;

import com.google.gson.JsonArray;
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

public class HttpClientSupplierSearch {

    public String search(String keyword, int page) {

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        String oneBoundApi = "https://api-gw.onebound.cn/1688/item_search_seller/?q=" + keyword + "&page=" + page +"&cache=no&lang=zh-CN&key=tel18606528273&secret=20200417";
        HttpPost httpPost = new HttpPost(oneBoundApi);
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

        return this.purify(responseStr);

    }

    public static void main(String[] args) {
        HttpClientSupplierSearch searcher = new HttpClientSupplierSearch();
        String str = searcher.search("男",11);
        System.out.println(searcher.purify(str));
    }


    private String purify(String json){

        if(json == null){
            return null;
        }
        JsonParser parser;
        parser = new JsonParser();
        JsonElement jsonElement = parser.parse(json);
        if (jsonElement instanceof JsonObject) {
            JsonObject  jsonObject = jsonElement.getAsJsonObject();
            return jsonObject.get("items").getAsJsonObject().get("item").toString();
        } else if (jsonElement instanceof JsonArray) {
            JsonArray  jsonArray = jsonElement.getAsJsonArray();
            return jsonArray.toString();
        }

        return null;
    }
}
