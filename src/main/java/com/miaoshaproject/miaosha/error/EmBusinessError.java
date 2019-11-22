package com.miaoshaproject.miaosha.error;

public enum EmBusinessError implements CommonError {
    //通用错误类型
    PARAMETER_INVALIDATION_ERROR(10001,"参数不合法"),
    UNKNOWN_ERROR(10002, "未知错误"),


    //业务错误
    USER_NOT_EXIST(20001, "用户不存在"),
    USER_LOGIN_FAIL(20002,"用户名或密码错误"),
    USER_NOT_LOGIN(20003,"用户名未登陆"),

    //交易错误
    STOCK_NOT_ENOUGH(30001,"库存不足")
    ;



    EmBusinessError(int errCode, String errMsg) {
        ErrCode = errCode;
        ErrMsg = errMsg;
    }

    private int ErrCode;
    private String ErrMsg;

    @Override
    public int getErrCode() {
        return this.ErrCode;
    }

    @Override
    public String getErrMsg() {
        return this.ErrMsg;
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.ErrMsg = errMsg;
        return this;
    }
}
