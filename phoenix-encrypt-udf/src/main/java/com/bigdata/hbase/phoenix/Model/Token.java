package com.bigdata.hbase.phoenix.Model;


import com.fasterxml.jackson.annotation.JsonProperty;

public class Token {
    @JsonProperty("token")
    private String token;

    public String getToken() {
        return token;
    }
}
