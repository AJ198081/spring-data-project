package dev.aj.data.aspects;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.time.StopWatch;

@Aspect
@Component
@Slf4j
public class PerformanceAspect {

    NumberFormat formatter = NumberFormat.getInstance(Locale.ENGLISH);

    @Pointcut(value = "@annotation(dev.aj.data.aspects.LogTiming)")
    public void performanceTiming() {}

    @SneakyThrows
    @Around(value = "performanceTiming()")
    public Object timingAdvice(ProceedingJoinPoint proceedingJoinPoint) {
        LogTiming logTimingAnnotation = ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod()
                                                                                     .getAnnotation(LogTiming.class);
        if (Objects.isNull(logTimingAnnotation)) {
            return proceedingJoinPoint.proceed();
        } else {
            String message = logTimingAnnotation.info();
            StopWatch stopWatch = new StopWatch(message);
            stopWatch.start();
            Object response = proceedingJoinPoint.proceed();
            stopWatch.stop();
            TimeUnit timeUnit = logTimingAnnotation.displayPerformanceInTimeUnit();
            String displayMessage = StringTemplate.STR."""
                    \{message} took \{formatter.format(stopWatch.getTime(timeUnit))} \{timeUnit} in total.
                    """;
            log.info(displayMessage);
            return response;
        }
    }
}
