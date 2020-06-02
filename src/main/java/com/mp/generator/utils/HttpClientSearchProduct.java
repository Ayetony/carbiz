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

public class HttpClientSearchProduct {


    public static String search(String keyword, int page) {

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        String oneBoundApi = "https://api.onebound.cn/1688/api_call.php?key=tel18606528273&secret=20200417&api_name=item_search&q="+keyword+"&start_price=0&end_price=0&page="+ page +"&cat=0&discount_only=&sort=&page_size=40&cache=no&seller_info=no&nick=&seller_info=&nick=&ppath=&imgid=&filter=";



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

        return responseStr;

    }

    private static JsonElement purify(String json){

        if(json == null){
            return null;
        }
        JsonParser parser;
        parser = new JsonParser();
        JsonObject jsonObject = parser.parse(json).getAsJsonObject();
        if(jsonObject.isJsonObject()){
            return jsonObject.get("items");
        }
        return null;
    }

    //搜索接口分类

    public static void main(String[] args) {
//        System.out.println(search("猫咪用品",2));
        String json = search("猫咪用品",4);
        JsonArray array = purify(json).getAsJsonObject().get("item").getAsJsonArray();
        System.out.println(array);

//        array.forEach(item -> System.out.println(item.getAsJsonObject().toString()));
    }


}
