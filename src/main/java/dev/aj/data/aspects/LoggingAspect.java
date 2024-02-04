package dev.aj.data.aspects;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.aj.data.domain.model.Model;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
@Order(1)
public class LoggingAspect {

    private final ObjectMapper objectMapper;

    @Pointcut("execution(public * dev.aj.data.controllers.ModelController.persistCurrentModel(..))")
    public void logModelBeingPersisted() {
    }

    @SneakyThrows
    @Around(value = "logModelBeingPersisted()")
    public Object adviseModelCreation(ProceedingJoinPoint proceedingJoinPoint) {
        if (proceedingJoinPoint.getArgs().length > 0) {
            log.info("Model received:%n%s".formatted(objectMapper.writeValueAsString(proceedingJoinPoint.getArgs()[0])));
        }

        Object response = proceedingJoinPoint.proceed();

        if (response instanceof ResponseEntity<?> responseEntity) {
            log.info("Model persisted:%n%s".formatted(responseEntity.getBody()));
        }

        return response;
    }

    @Pointcut("execution(public * dev.aj.data.controllers.ModelController.getAModel(..))")
    public void logGetAModel() {
    }

    @SneakyThrows
    @Around(value = "logGetAModel()")
    public Object adviseFetchingAModel(ProceedingJoinPoint proceedingJoinPoint) {
        if (proceedingJoinPoint.getArgs().length > 0) {
            Object identifier = proceedingJoinPoint.getArgs()[0];
            log.info("Fetching Model for Id:[%s]".formatted(objectMapper.writeValueAsString(identifier)));
        }

        Object response = proceedingJoinPoint.proceed();

        if (response instanceof ResponseEntity<?> responseEntity) {
            log.info("Model fetched:%n%s".formatted(responseEntity.getBody()));
        }

        return response;
    }
}
