package com.example.watchApp.pizzawatchface.http;

import java.io.Serializable;

public class HttpMultipartData implements Serializable{

    public static final int TYPE_FILE = 1;
    public static final int TYPE_DATA = 2;

    public static final int FILE_TYPE_PRIVATE = 1;
    public static final int FILE_TYPE_PUBLIC = 2;

    private int type;
    private String name;
    private String filePath;
    private int fileType;
    private byte[] data;

    public HttpMultipartData(String name, int fileType, String filePath){
        this.type = TYPE_FILE;
        this.name = name;
        this.fileType = fileType;
        this.filePath = filePath;
    }

    public HttpMultipartData(String name, byte[] data){
        this.type = TYPE_DATA;
        this.name = name;
        this.data = data;
    }

    public int getType(){
        return type;
    }

    public void setType(int type){
        this.type = type;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getFilePath(){
        return filePath;
    }

    public void setFilePath(String filePath){
        this.filePath = filePath;
    }

    public int getFileType(){
        return fileType;
    }

    public void setFileType(int fileType){
        this.fileType = fileType;
    }

    public byte[] getData(){
        return data;
    }

    public void setData(byte[] data){
        this.data = data;
    }
}
