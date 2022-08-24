package com.bigdata.hbase.phoenix.util;


import com.bigdata.hbase.phoenix.Model.Response;
import com.bigdata.hbase.phoenix.Model.Token;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;


import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


public class Helper {
    public final Long cachedItemsExpiry = 1L;

    private final Configuration conf =  HBaseConfiguration.create();
    public  volatile  String base_url = conf.get("kms.api.url");

    public  volatile String bearerAuth;

    public  Helper(){
        this.getBearerToken();
    }

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

        String aesKey = "unauthorized";
        try {
            String url = String.format(base_url + "/api/v1/key?id=%s&username=%s", id, username);
            HttpGet request = new HttpGet(url);
            request.addHeader("Authorization", this.bearerAuth);


            CloseableHttpClient httpclient = HttpClients.custom().build();

            CloseableHttpResponse closeableHttpResponse = httpclient.execute(request);

            try {
                if (closeableHttpResponse.getStatusLine().getStatusCode() != 200) {
                    System.out.println(closeableHttpResponse.getStatusLine().getStatusCode());
                    return aesKey;
                } else {
                    HttpEntity entity = closeableHttpResponse.getEntity();
                    ObjectMapper mapper = new ObjectMapper();
                    Response response = mapper.readValue(EntityUtils.toString(entity), Response.class);
                    aesKey = response.getKey();
                }
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            } finally {
                closeableHttpResponse.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
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

    public  void getBearerToken(){


        try {
            String authUsername = conf.get("kms.api.auth.username");
            String authPassword = conf.get("kms.api.auth.password");
            String url = String.format(base_url + "/access/token");

            String creds = "Basic "+ Base64.getEncoder().encodeToString( (authUsername+":"+authPassword).getBytes());
            HttpPost request = new HttpPost(url);
            request.addHeader("Authorization", creds);

            CloseableHttpClient httpclient = HttpClients.custom().build();

            CloseableHttpResponse closeableHttpResponse = httpclient.execute(request);

            try {
                if (closeableHttpResponse.getStatusLine().getStatusCode() != 200) {
                    System.out.println(closeableHttpResponse.getStatusLine().getStatusCode());
                    this.bearerAuth= "Wrong kms user Credentials";
                } else {
                    HttpEntity entity = closeableHttpResponse.getEntity();
                    ObjectMapper mapper = new ObjectMapper();
                    Token response = mapper.readValue(EntityUtils.toString(entity), Token.class);
                    this.bearerAuth= "Bearer "+ response.getToken();
                }
            } catch (IOException | ParseException e) {
                this.bearerAuth= "Get token Error";
                e.printStackTrace();
            } finally {
                closeableHttpResponse.close();
                this.bearerAuth= "Get token Error";
            }
        } catch (IOException e) {
            e.printStackTrace();
            this.bearerAuth= "Get token Error";
        }

    }

}
