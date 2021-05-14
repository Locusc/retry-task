package cn.locusc.retry.task.components;

import cn.locusc.retry.task.annotation.RetryHandler;
import cn.locusc.retry.task.domain.RetryTask;
import cn.locusc.retry.task.mapper.RetryTaskMapper;
import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * @author jayChan
 * 重试组件
 * 2021/4/9
 **/
@Slf4j
@Component
public class RetryHandlerComponent {

    @Resource
    private RetryTaskMapper retryTaskMapper;

    public void afterThrowing(JoinPoint joinPoint, RetryHandler retryHandler, Throwable throwable) {

        String methodPath = String.format("%s.%s",
                joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());

        // log.info("RetryHandler获取到的异常信息, 【{}】", this.exceptionJson(retryHandler, throwable).toJSONString());
        log.info("RetryHandler获取到的当前方法的路径【{}】", methodPath);
        // log.info("RetryHandler获取到的当前方法的入参信息, 【{}】", this.methodInfoJson(joinPoint));

        log.info("RetryHandler获取到的异常信息: ", throwable);

        // 判断该异常是否是需要重试的异常
        boolean contains = Arrays.asList(retryHandler.value()).contains(throwable.getClass());
        if(!contains) {
            return;
        }

        // 判断是否需要进行同步重试
        if(retryHandler.isSyncRetry()) {
            this.syncRetry(joinPoint, retryHandler, throwable);
        } else {
            this.asyncRetry(joinPoint, retryHandler, throwable);
        }
    }

    public void syncRetry(JoinPoint joinPoint, RetryHandler retryHandler, Throwable throwable) {
        // todo 同步重试待实现
        int maxAttempts = retryHandler.maxAttempts();
        String methodPath = joinPoint.getSignature().getDeclaringTypeName()
                + "." +joinPoint.getSignature().getName();
    }

    /**
     * 异步重试方法 定时执行
     * @param joinPoint 连接点
     * @param retryHandler 注解参数
     * @param throwable 错误信息
     **/
    public void asyncRetry(JoinPoint joinPoint, RetryHandler retryHandler, Throwable throwable) {
        RetryTask retryTask = new RetryTask();

        Date date = new Date();
        retryTask.setIsDel(0);
        // 任务名称
        retryTask.setTaskName(retryHandler.taskName());
        // 方法信息
        retryTask.setMethodInfo(this.methodInfoJson(joinPoint).toJSONString());

        // 判断任务是否以及存在 已存在的任务使用历史任务
        List<RetryTask> retryTasks = retryTaskMapper.queryRetryTaskListByEntity(retryTask);

        if(CollectionUtils.isNotEmpty(retryTasks)) {
            RetryTask retryTask1 = retryTasks.get(NumberUtils.INTEGER_ZERO);
            if(NumberUtils.INTEGER_ZERO.equals(retryTask1.getRetryStatus())) {
                if(retryTask1.getMaxAttempts() > retryTask1.getCurrentAttempts()) {
                    // 重试状态为失败 最大次数大于当前次数 正在执行的错误
                    log.error("RetryHandler获取到了TaskName和入参信息相同的【正在执行】的任务, 将不新增重试任务");
                    return;
                }
            } else {
                // 重试状态为成功 最大次数等于当前次数 未在执行的历史任务
                retryTask1.setRetryStatus(NumberUtils.INTEGER_ZERO);
            }
            // 重试状态为失败 最大次数等于当前次数 未在执行的历史任务
            retryTask1.setMaxAttempts(retryHandler.maxAttempts());
            retryTask1.setCurrentAttempts(NumberUtils.INTEGER_ZERO);
            retryTask1.setUpdateDate(date);
            retryTask1.setMaxInterval(retryHandler.maxInterval());
            retryTask1.setRetryRule(retryHandler.rule());
            retryTask1.setExceptionJson(this.exceptionJson(retryHandler, throwable).toJSONString());
            retryTaskMapper.updateByPrimaryKeySelective(retryTask1);
            log.error("RetryHandler获取到了TaskName和入参信息相同的【历史】任务, 重置异步重试参数");
            return;
        }

        // 错误信息
        retryTask.setExceptionJson(this.exceptionJson(retryHandler, throwable).toJSONString());
        // 最大重试次数
        retryTask.setMaxAttempts(retryHandler.maxAttempts());
        // 最大休眠时间
        retryTask.setMaxInterval(retryHandler.maxInterval());
        // 创建时间
        retryTask.setCreateDate(date);
        // 当前重试状态
        retryTask.setRetryStatus(0);
        // 当前重试次数
        retryTask.setCurrentAttempts(0);
        // 请求入参
        // retryTask.setRetryParams(this.methodParamsJson(joinPoint).toJSONString());
        // 重试规则
        retryTask.setRetryRule(retryHandler.rule());

        retryTaskMapper.insertSelective(retryTask);
    }

    /**
     * 反射获取到执行的方法
     **/
    private Object reflectionMethod(JoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            boolean isStatic = Modifier.isStatic(signature.getMethod().getModifiers());
            Class<?> declaringType = joinPoint.getSignature().getDeclaringType();
            String name = joinPoint.getSignature().getName();
            Method method = declaringType.getMethod(name, signature.getParameterTypes());
            method.setAccessible(true);
            if(isStatic) {
                return method.invoke(declaringType, joinPoint.getArgs());
            } else {
                Constructor<?> constructor = declaringType.getConstructor();
                Object o = constructor.newInstance();
                return method.invoke(o, joinPoint.getArgs());
            }
        } catch (NoSuchMethodException e) {
            log.error("RetryHandler通过反射获取重试的方法错误, {}", e.getMessage(), e);
        } catch (IllegalAccessException e) {
            log.error("RetryHandler通过反射执行重试的方法非法访问错误, {}", e.getMessage(), e);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            log.error("RetryHandler通过反射执行重试的方法错误, {}", targetException.getMessage(), targetException);
        } catch (InstantiationException e) {
            log.error("RetryHandler通过反射获取方法所在类实例错误, {}", e.getMessage(), e);
        }
        return null;
    }

    private JSONObject exceptionJson(RetryHandler retryHandler, Throwable throwable) {
        // 当前错误名称
        String currentException = throwable.getClass().getName();
        // 当前错误信息
        String currentExceptionMessage = throwable.getMessage();
        // 需要重试的错误
        String retryException = JSONObject.toJSONString(Arrays.stream(retryHandler.value()).toArray());
        // 组装错误信息
        JSONObject exceptionJson = new JSONObject();
        exceptionJson.put("currentException", currentException);
        exceptionJson.put("currentExceptionMessage", currentExceptionMessage);
        exceptionJson.put("retryException", retryException);
        return exceptionJson;
    }

    private JSONObject methodInfoJson(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        // 入参名称
        String[] argNames = signature.getParameterNames();
        // 入参类型
        Class<?>[] parameterTypes = signature.getParameterTypes();
        // 组装方法信息
        JSONObject methodInfoJson = new JSONObject();
        // 组装入参信息
        LinkedHashMap<String, Object> methodParamsMap = new LinkedHashMap<>();

        Stream.iterate(0, i -> i + 1).limit(argNames.length).forEach(i -> {
            LinkedHashMap<String, Object> argJson = new LinkedHashMap<>();

            argJson.put("argType", parameterTypes[i]);
            argJson.put("argInfo", joinPoint.getArgs()[i]);

            // 泛型类型
            Type genericParameterType = signature.getMethod().getGenericParameterTypes()[i];
            String s = this.actualTypeArgument(genericParameterType);
            if(StringUtils.isNotEmpty(s)) {
                argJson.put("actualTypeName", s);
            }
            methodParamsMap.put(argNames[i], argJson);
        });
        methodInfoJson.put("classPath", joinPoint.getSignature().getDeclaringTypeName());
        methodInfoJson.put("methodName", joinPoint.getSignature().getName());
        methodInfoJson.put("methodParams", methodParamsMap);
        return methodInfoJson;
    }

    /**
     * 集合泛型类型转换
     * @param genericParameterType 入参类型
     * @return 泛型类型字符
     **/
    public String actualTypeArgument(Type genericParameterType) {
        if(genericParameterType == null) {
            return null;
        }

        ParameterizedType parameterizedType;
        try {
            parameterizedType = (ParameterizedType) genericParameterType;
        } catch (ClassCastException e) {
            return null;
        }
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if(actualTypeArguments.length <= NumberUtils.INTEGER_ZERO) {
            return null;
        }

        // todo 泛型类型应该递归获取 复杂类型(或者类中包含复杂类型): ArrayList<List<Map<TreeMap<String, Object>, Integer>>>
        AtomicInteger atomicInteger = new AtomicInteger();
        String [] actualTypeNames = new String[actualTypeArguments.length];
        Arrays.stream(actualTypeArguments).forEach(value ->
                actualTypeNames[atomicInteger.getAndIncrement()] = value.getTypeName()
        );
        return StringUtils.join(actualTypeNames, ",");
    }

}
