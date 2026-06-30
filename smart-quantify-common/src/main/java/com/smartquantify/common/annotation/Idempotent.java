package com.smartquantify.common.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    String key() default "";

    long expireTime() default 60;

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    String prefix() default "idempotent:";

    String errorMessage() default "Request has been processed, please try again later";
}
