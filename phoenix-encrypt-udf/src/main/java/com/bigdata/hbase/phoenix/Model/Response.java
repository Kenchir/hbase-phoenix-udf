package com.bigdata.hbase.phoenix.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
//import com.google.api.client.util.Key;

public class Response {

    @JsonProperty("identifier")
    private String identifier;
    @JsonProperty("key")
    private String key;

    @JsonProperty("expired")
    private boolean expired;

//    @JsonProperty("username")
//    private  String username;

    public String getKey() {
        return key;
    }

//    private static class  Data{
//        @JsonProperty("identifier")
//        private String identifier;
//        @JsonProperty("key")
//        private String key;
//
//        @JsonProperty("expired")
//        private boolean expired;
//
//        @JsonProperty("username")
//        private  String username;
//    }

}
