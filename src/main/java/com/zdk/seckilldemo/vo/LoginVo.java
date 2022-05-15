package com.zdk.seckilldemo.vo;

import com.zdk.seckilldemo.validator.IsMobile;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @author zdk
 * @date 2022/5/15 17:25
 */
@Data
public class LoginVo {
    @NotBlank(message = "手机号不能为空")
    @IsMobile
    private String mobile;

    @NotBlank(message = "密码不能为空")
    @Length(min = 32)
    /**
     * 可以用Pattern正则对手机号进行验证，如果用了，在不满足时，会抛出BindException
     * 此时需要我们手动处理(使用@RestControllerAdvice)，否则前端就得不到具体的响应
     *
     * 当然 也可以自定义注解
     */
//    @Pattern(regexp = "[1]([3-9])[0-9]{9}$")
    private String password;
}
