package dev.aj.data.aspects;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.TYPE})
public @interface LogTiming {
    String info() default "";
    TimeUnit displayPerformanceInTimeUnit() default TimeUnit.MILLISECONDS;
}
