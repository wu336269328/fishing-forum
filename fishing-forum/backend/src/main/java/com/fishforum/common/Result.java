package com.fishforum.common;

import lombok.Data;

/**
 * 统一响应结果封装
 */
@Data
public class Result<T> {
    private int code; // 状态码
    private String message; // 提示信息
    private T data; // 数据

    // 成功（带数据）
    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage("操作成功");
        r.setData(data);
        return r;
    }

    // 成功（带消息和数据）
    public static <T> Result<T> ok(String message, T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage(message);
        r.setData(data);
        return r;
    }

    // 成功（无数据）
    public static <T> Result<T> ok() {
        return ok(null);
    }

    // 失败
    public static <T> Result<T> error(String message) {
        Result<T> r = new Result<>();
        r.setCode(500);
        r.setMessage(message);
        return r;
    }

    // 失败（自定义状态码）
    public static <T> Result<T> error(int code, String message) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }
}
