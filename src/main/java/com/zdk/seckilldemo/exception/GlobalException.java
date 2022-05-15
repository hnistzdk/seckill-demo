package com.zdk.seckilldemo.exception;

import com.zdk.seckilldemo.vo.ApiRespEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zdk
 * @date 2022/5/15 18:13
 * 全局异常类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GlobalException extends RuntimeException{
    private ApiRespEnum apiRespEnum;
}
