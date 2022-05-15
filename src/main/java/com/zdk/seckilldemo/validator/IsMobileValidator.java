package com.zdk.seckilldemo.validator;

import com.zdk.seckilldemo.utils.ValidatorUtil;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author zdk
 * @date 2022/5/15 19:41
 */
public class IsMobileValidator implements ConstraintValidator<IsMobile,String> {
    private boolean required = false;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        //如果不是必填的 并且为空 直接返回true
        if (!required && StringUtils.isBlank(value)){
            return true;
        }
        //如果必填 或者非必填时它填了  就需要校验
        return ValidatorUtil.isMobile(value);
    }

    @Override
    public void initialize(IsMobile constraintAnnotation) {
        required = constraintAnnotation.required();
    }
}
