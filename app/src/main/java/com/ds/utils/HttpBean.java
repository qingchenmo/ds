package com.ds.utils;

public class HttpBean<T> {

    /**
     * code : 200
     * msg : 专用性地锁设备码:54203112230,车牌号:湘A888888
     * data : null
     */

    private int code;
    private String msg;
    private T data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
