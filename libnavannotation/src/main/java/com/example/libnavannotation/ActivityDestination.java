package com.example.libnavannotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface ActivityDestination {
    String pageUrl();  //作为页面表示

    boolean needLogin() default false;  //是否在登陆以后才能显示

    boolean asStarter() default false;  //是否是默认启动

}
