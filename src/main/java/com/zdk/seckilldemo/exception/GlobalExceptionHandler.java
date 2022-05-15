package com.zdk.seckilldemo.exception;

import com.zdk.seckilldemo.vo.ApiResp;
import com.zdk.seckilldemo.vo.ApiRespEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author zdk
 * @date 2022/5/15 18:13
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ApiResp handle(Exception e){
        //处理自定义的异常
        if (e instanceof GlobalException){
            GlobalException exception = (GlobalException) e;
            return ApiResp.error(exception.getApiRespEnum());
        }
        //处理使用javax的Validator的异常
        else if(e instanceof BindException) {
            BindException bindException = (BindException) e;
            ApiResp respBean = ApiResp.error(ApiRespEnum.BIND_ERROR);
            respBean.setMessage("参数校验异常：" + bindException.getBindingResult().getAllErrors().get(0).getDefaultMessage());
            return respBean;
        }
        e.printStackTrace();
        log.error("异常信息:{}",e.getMessage());
        return ApiResp.error(ApiRespEnum.ERROR);
    }

}
