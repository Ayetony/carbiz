package com.mp.generator.httpClientPooling;

import org.apache.http.HttpHost;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.util.concurrent.TimeUnit;

public class HttpClientManager {

    public static CloseableHttpClient httpClientInstance(){

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
     // Increase max total connection to 200
        cm.setMaxTotal(200);
     // Increase default max connection per route to 20
        cm.setDefaultMaxPerRoute(20);
     // Increase max connections for localhost:80 to 50
        HttpHost host = new HttpHost("https://api-gw.onebound.cn", 80);
        cm.setMaxPerRoute(new HttpRoute(host), 50);
        cm.closeIdleConnections(30, TimeUnit.SECONDS);
        cm.closeExpiredConnections();
        return HttpClients.custom()
                .setConnectionManager(cm)
                .build();
    }

}
