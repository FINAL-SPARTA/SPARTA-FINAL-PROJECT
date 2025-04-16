package com.fix.order_service.application.aop;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidateUser {
    String[] roles() default {};
}