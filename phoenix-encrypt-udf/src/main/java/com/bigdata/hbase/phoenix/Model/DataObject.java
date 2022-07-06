package com.bigdata.hbase.phoenix.Model;

public class DataObject {
    private final String data;

    private static int objectCounter = 0;

    DataObject(String data) {
        this.data = data;
    }
    // standard constructors/getters

    public static DataObject get(String data) {
        objectCounter++;
        return new DataObject(data);
    }
}
