package com.zdk.seckilldemo.vo;

/**
 * @author zdk
 * @date 2022/5/15 17:22
 */
public class ApiResp {

    private long code;
    private String message;
    private Object object;

    public static ApiResp success() {
        return new ApiResp(ApiRespEnum.SUCCESS.getCode(), ApiRespEnum.SUCCESS.getMessage(), null);
    }

    public static ApiResp success(boolean result) {
        if (result){
            return new ApiResp(ApiRespEnum.SUCCESS.getCode(), ApiRespEnum.SUCCESS.getMessage(), null);
        }else{
            return new ApiResp(ApiRespEnum.ERROR.getCode(), ApiRespEnum.ERROR.getMessage(), null);
        }
    }

    public static ApiResp success(Object object) {
        return new ApiResp(ApiRespEnum.SUCCESS.getCode(), ApiRespEnum.SUCCESS.getMessage(), object);
    }

    public static ApiResp error(ApiRespEnum apiRespEnum) {
        return new ApiResp(apiRespEnum.getCode(), apiRespEnum.getMessage(), null);
    }

    public static ApiResp error(ApiRespEnum apiRespEnum, Object object) {
        return new ApiResp(apiRespEnum.getCode(), apiRespEnum.getMessage(), object);
    }

    public boolean isSuccess(ApiResp apiResp){
        return apiResp.code == ApiRespEnum.SUCCESS.getCode();
    }

    public boolean isFail(ApiResp apiResp){
        return !isSuccess(apiResp);
    }

    public ApiResp(long code, String message, Object object) {
        this.code = code;
        this.message = message;
        this.object = object;
    }

    public ApiResp(){

    }

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
