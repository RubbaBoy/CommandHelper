package com.uddernetworks.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Command {
    String name();
    String[] aliases() default "";
    String permission() default "";
    boolean consoleAllow() default true;
    int minArgs() default -1;
    int maxArgs() default -1;
}