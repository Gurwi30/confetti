package dev.gurwi.confetti.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Config {

    String value();

    boolean resourceConfig() default false;

    String resourcePath() default "";

    boolean autoSave() default false;

}
