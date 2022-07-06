package com.bigdata.hbase.phoenix.util;


import com.bigdata.hbase.phoenix.Model.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Helper {
    public final Long cachedItemsExpiry = 1L;

    private final Configuration conf = new Configuration();
    public final LoadingCache<String, String> aesKeyCache = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(cachedItemsExpiry, TimeUnit.DAYS)
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(String key) {
                    String[] parts = key.split("_");
                    return getKeyFromHttp(parts[0], parts[1]);
                }
            });

    public String getKeyFromHttp(String username, String id) {

        String aesKey = "Invalid";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            String base_url = conf.get("kms.api.url");
            String auth_token = conf.get("kms.api.auth.token");

            String url = String.format(base_url + "?id=%s&username=%s", id, username);
            HttpGet request = new HttpGet(url);
            request.addHeader("Authorization", auth_token);
            CloseableHttpResponse closeableHttpResponse = httpClient.execute(request);

            try {
                if (closeableHttpResponse.getStatusLine().getStatusCode() != 200) {
                    return "Invalid";
                } else {
                    HttpEntity entity = closeableHttpResponse.getEntity();
                    ObjectMapper mapper = new ObjectMapper();
                    Response response = mapper.readValue(EntityUtils.toString(entity), Response.class);
                    aesKey = response.getKey();
                    return aesKey;
                }
            } catch (IOException | ParseException e) {

                e.printStackTrace();
                return "Invalid";
            } finally {
                closeableHttpResponse.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {

                httpClient.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return aesKey;
    }

    public String getKeyFromCache(String username, String id) {

        String cacheKeyName = username + "_" + id;
        try {
            return this.aesKeyCache.get(cacheKeyName);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return "Invalid";
        }
    }

}
