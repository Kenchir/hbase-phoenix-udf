package com.bigdata.hbase.phoenix.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public  class AesEncryption  {



    public  EncryptionAlgorithmImpl encryptionAlgorithm;
    public AesEncryption(){
        this.encryptionAlgorithm = new EncryptionAlgorithmImpl();
    }


    volatile  Long cachedItemsExpiry = 1L;


    volatile LoadingCache<String, String> plainTextCache = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(cachedItemsExpiry, TimeUnit.DAYS)
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(String key) {
                    String[] parts = key.split("_");
                    return encryptionAlgorithm.decrypt(parts[0],parts[1],parts[2]);
                }
            });


    volatile LoadingCache<String, String> cipherTextCache = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(cachedItemsExpiry, TimeUnit.DAYS)
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(String key) {
                    String[] parts = key.split("_");
                    return encryptionAlgorithm.encrypt(parts[0],parts[1],parts[2]);
                }
            });


    public   String decrypt(String algorithm,String cipherText, String aesKey){
        String cacheKeyName = algorithm + "_"+ cipherText + "_" + aesKey;
        try {
            return plainTextCache.get(cacheKeyName);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return  "Invalid";
        }
    }


    public   String encrypt(String algorithm,String plainText, String aesKey){
        String cacheKeyName = algorithm + "_"+ plainText + "_" + aesKey;
        try {
            return cipherTextCache.get(cacheKeyName);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return  "Invalid";
        }
    }

}
