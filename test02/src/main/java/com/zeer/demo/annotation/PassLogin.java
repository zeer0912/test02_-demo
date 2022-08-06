package com.zeer.demo.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
//注解不仅被保存到class文件中，jvm加载class文件之后，仍然存在；
public @interface PassLogin {
    boolean required() default true;//默认为真
}