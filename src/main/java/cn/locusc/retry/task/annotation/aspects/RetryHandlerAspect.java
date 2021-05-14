package cn.locusc.retry.task.annotation.aspects;

import cn.locusc.retry.task.annotation.RetryHandler;
import cn.locusc.retry.task.components.RetryHandlerComponent;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author jayChan
 * 重试切面
 * 2021/4/21
 **/
@Aspect
@Component
@EnableAspectJAutoProxy(exposeProxy = true)
public class RetryHandlerAspect {

    @Resource
    private RetryHandlerComponent retryHandlerComponent;

    @Pointcut("@annotation(com.hrfax.presys.busi.retry.annotation.RetryHandler)")
    private void pointcutMethod() { }

    @Around(value = "@annotation(retryHandler)")
    public Object around(ProceedingJoinPoint proceedingJoinPoint, RetryHandler retryHandler) throws Throwable {
        return proceedingJoinPoint.proceed();
    }

    @AfterReturning(value = "@annotation(retryHandler)", returning = "value")
    public void afterReturning(JoinPoint joinPoint, Object value, RetryHandler retryHandler) {
    }

    @AfterThrowing(value = "@annotation(retryHandler)", throwing = "throwable")
    public void afterThrowing(JoinPoint joinPoint, RetryHandler retryHandler,
                              Throwable throwable) {
        retryHandlerComponent.afterThrowing(joinPoint, retryHandler, throwable);
    }

}