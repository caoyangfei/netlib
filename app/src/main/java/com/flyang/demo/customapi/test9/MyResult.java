package com.flyang.demo.customapi.test9;


import com.flyang.network.model.ApiResult;

public class MyResult<T> extends ApiResult<T> {

    int flag;
    String message;
    //T data;
    @Override
    public int getCode() {
        return flag;
    }

    @Override
    public String getMsg() {
        return message;
    }

   /* @Override
    public T getData() {
        return data;
    }*/

    @Override
    public boolean isOk() {
        return flag == 100;
    }
}
