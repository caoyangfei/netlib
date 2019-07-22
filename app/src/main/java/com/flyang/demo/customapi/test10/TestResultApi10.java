package com.flyang.demo.customapi.test10;


import com.flyang.netlib.model.ApiResult;

public class TestResultApi10<T> extends ApiResult<T> {
    private int errorCode;
    private String errorMsg;

    @Override
    public String getMsg() {
        return errorMsg;
    }

    @Override
    public int getCode() {
        return errorCode;
    }

    @Override
    public boolean isOk() {
        //return getCode()==0;
        //或者
        return errorCode ==0;
    }
}
