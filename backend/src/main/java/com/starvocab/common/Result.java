package com.starvocab.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应体（紫色星球主题，message 采用星空语境）
 */
@Data
public class Result<T> implements Serializable {

    private int code;
    private String message;
    private T data;

    public static <T> Result<T> ok() {
        Result<T> r = new Result<>();
        r.code = 0;
        r.message = "星辰已点亮";
        return r;
    }

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.code = 0;
        r.message = "星辰已点亮";
        r.data = data;
        return r;
    }

    public static <T> Result<T> ok(String message, T data) {
        Result<T> r = new Result<>();
        r.code = 0;
        r.message = message;
        r.data = data;
        return r;
    }

    public static <T> Result<T> fail(int code, String message) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        return r;
    }
}
