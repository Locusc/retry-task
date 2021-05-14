package cn.locusc.retry.task.service.impl;

import cn.locusc.retry.task.domain.RetryTask;
import cn.locusc.retry.task.mapper.RetryTaskMapper;
import cn.locusc.retry.task.service.RetryHandlerService;
import cn.locusc.retry.task.utils.DateUtil;
import cn.locusc.retry.task.utils.JacksonUtils;
import cn.locusc.retry.task.utils.SpringUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jayChan
 * 重试任务实现
 * 2021/4/15
 **/
@Slf4j
@Service
public class RetryHandlerServiceImpl implements RetryHandlerService {

    @Resource
    private RetryTaskMapper retryTaskMapper;

    @Resource
    private SpringUtil springUtil;

    @Override
    public void executorAsyncRetry() {
        List<RetryTask> retryTasks = retryTaskMapper.queryRetryTaskList();

        if(CollectionUtils.isEmpty(retryTasks)) {
            return;
        }

        retryTasks.forEach(item -> {

            log.info("异步重试任务正在执行【{}】, 创建时间【{}】", item.getTaskName(), DateUtil.ymdhmsFormat(item.getCreateDate()));

            // 错误信息
            JSONObject exceptionJson = JSONObject.parseObject(item.getExceptionJson());
            // 方法信息
            JSONObject methodInfoJson = JSONObject.parseObject(item.getMethodInfo(), Feature.OrderedField);
            // 类路径
            String classPath = methodInfoJson.getString("classPath");
            // 方法名称
            String methodName = methodInfoJson.getString("methodName");
            // 入参信息
            JSONObject methodParamsJson = methodInfoJson.getJSONObject("methodParams");

            Object [] methodParamsArray = new Object[methodParamsJson.size()];
            Class<?> [] argTypeArray = new Class[methodParamsJson.size()];

            AtomicInteger atomicInteger = new AtomicInteger();
            methodParamsJson.forEach((key, value) -> {
                JSONObject argsJson = JSONObject.parseObject(JSONObject.toJSONString(value), Feature.OrderedField);
                try {
                    // 入参类型
                    String argType = argsJson.getString("argType");
                    // 获取方法执行类的实例
                    Class<?> argTypeClass = Class.forName(argType);

                    Object o = this.methodArgumentsConstructorPattern(argsJson, argTypeClass);
                    if (o == null) {
                        log.error("异步重试任务获取【实参】信息错误, 返回的实参为空, 入参名【{}】, 入参信息【{}】"
                                , key, argsJson.toJSONString());
                        return;
                    }

                    int currentAtomicInteger = atomicInteger.getAndIncrement();
                    // 实参组装
                    methodParamsArray[currentAtomicInteger] = o;
                    // 参数类型组装
                    argTypeArray[currentAtomicInteger] = argTypeClass;

                } catch (ClassNotFoundException e) {
                    log.error("异步重试任务获取【入参类】信息错误, 类信息不存在, {}", e.getMessage(), e);
                }
            });

            // 执行重试的方法
            this.executorRetryMethod(classPath, methodName, argTypeArray,
                    methodParamsArray, exceptionJson, item);
        });

    }

    /**
     * 更新任务信息
     * @param retryTask 任务实体
     **/
    private void afterUpgradeTask(RetryTask retryTask) {
        RetryTask upgradeTask = new RetryTask();
        upgradeTask.setId(retryTask.getId());
        upgradeTask.setCurrentAttempts(retryTask.getCurrentAttempts() + NumberUtils.INTEGER_ONE);
        upgradeTask.setUpdateDate(new Date());
        upgradeTask.setRetryStatus(retryTask.getRetryStatus());
        upgradeTask.setResponseData(retryTask.getResponseData());
        retryTaskMapper.updateByPrimaryKeySelective(upgradeTask);
    }

    /**
     * 实参生成(构造器转换模式)
     * @param argsJson 入参信息json
     * @param argTypeClass 入参类型
     * @return java.lang.Object
     **/
    private Object methodArgumentsConstructorPattern(JSONObject argsJson, Class<?> argTypeClass) {

        if (!argsJson.containsKey("argInfo") || StringUtils.isBlank(argsJson.getString("argInfo"))) {
            return null;
        }

        // 实参字符
        String argInfo = argsJson.getString("argInfo");

        // 判断是否包含泛型参数
        if(argsJson.containsKey("actualTypeName")) {
            String actualTypeName = argsJson.getString("actualTypeName");
            String[] split = actualTypeName.split(",");

            Class<?> [] actualTypeClass = new Class[split.length];

            AtomicInteger atomicInteger = new AtomicInteger();
            Arrays.stream(split).forEach(value -> {
                int andIncrement = atomicInteger.getAndIncrement();
                try {
                    actualTypeClass[andIncrement] = Class.forName(value);
                } catch (ClassNotFoundException e) {
                    log.error("异步重试任务实参转换【泛型类型】错误, {}", e.getMessage(), e);
                }
            });
            return JacksonUtils.string2Obj(argInfo, argTypeClass, actualTypeClass);
        } else {
            return JacksonUtils.string2Obj(argInfo, argTypeClass);
        }
    }

    /**
     * 执行重试的方法
     * @param classPath 类路径
     * @param methodName 方法名
     * @param argTypeArray 入参类型数组
     * @param arguments 实参数组
     **/
    private void executorRetryMethod(String classPath, String methodName,
                                     Class<?> [] argTypeArray, Object[] arguments,
                                     JSONObject exceptionJson, RetryTask retryTask) {
        Method executorMethod = null;
        Class<?> executorClass = null;
        Object o = null;
        try {
            executorClass = Class.forName(classPath);
            Constructor<?> constructor = executorClass.getConstructor();
            o = constructor.newInstance();
            executorMethod = executorClass.getMethod(methodName, argTypeArray);
            executorMethod.setAccessible(true);
        } catch (ClassNotFoundException e) {
            log.error("异步重试任务获取【重试方法】【类】信息错误, 类信息不存在, {}", e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            log.error("异步重试任务获取【重试方法】信息错误, 方法信息不存在, {}", e.getMessage(), e);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.error("异步重试任务获取【重试方法】【类】实例错误, {}", e.getMessage(), e);
        }

        if(executorMethod == null) {
            log.error("异步重试任务执行【重试方法】前获取方法信息为空");
            return;
        }

        try {
            // 类名
            String className = StringUtils.substring(
                    classPath,
                    classPath.lastIndexOf(".") + NumberUtils.INTEGER_ONE
            );

            // 判断该重试方法类是否在spring中
            boolean b = springUtil.containsBean(StringUtils.uncapitalize(className));
            Object invoke;
            if(b) {
                invoke = executorMethod.invoke(SpringUtil.getBean(executorClass), arguments);
            } else {
                invoke = executorMethod.invoke(o, arguments);
            }

            // 更新任务信息
            retryTask.setRetryStatus(1);
            if(invoke != null) {
                retryTask.setResponseData(JSONObject.toJSONString(invoke));
            }
            this.afterUpgradeTask(retryTask);
        } catch (IllegalAccessException e) {
            log.error("异步重试任务执行【重试方法】非法访问错误, {}", e.getMessage(), e);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            String s = JSONObject.toJSONString(targetException.getStackTrace()[0]);
            log.error("异步重试任务执行【重试方法】错误, {}, 错误信息: {}", targetException.toString(), s);

            // 需要重试的错误
            JSONArray retryException = exceptionJson.getJSONArray("retryException");

            // todo 可判断是否为需要重试的错误
            Class<? extends Throwable> aClass = targetException.getClass();
            boolean contains = retryException.contains(aClass.getName());

            this.afterUpgradeTask(retryTask);
        }

    }

    /**
     * 实参生成(fastJson转换模式)
     * @param argsJson 入参信息json
     * @param argTypeClass 入参类型
     * @return java.lang.Object
     **/
    private Object methodArgumentsConstructorFastJson(JSONObject argsJson, Class<?> argTypeClass) {
        try {
            // 返回的实参
            Object o;
            // 入参类型
            String argType = argsJson.getString("argType");
            String actualTypeName = null;
            if(argsJson.containsKey("actualTypeName")) {
                actualTypeName = argsJson.getString("actualTypeName").split(",")[0];
            }

            if (argType.equals(String.class.getName()) ||
                    argType.equals(Integer.class.getName()) ||
                    argType.equals(BigDecimal.class.getName())
            ) {
                // 实参
                String argInfo = argsJson.getString("argInfo");
                Constructor<?> constructor = argTypeClass.getConstructor(argTypeClass);
                o = constructor.newInstance(argInfo);
            } else if(argType.equals(JSONObject.class.getName())) {
                o = JSONObject.parseObject(argsJson.getString("argInfo"), Feature.OrderedField);
            } else {
                // Constructor<?> constructor1 = argTypeClass.getConstructor();
                // o = constructor1.newInstance();
                // 实参
                // JSONObject argInfo = JSONObject
                //        .parseObject(argsJson.getString("argInfo"), Feature.OrderedField);

                // argInfo.forEach((key1, value1) ->
                //        this.reflectionArgumentsObject(argTypeClass, o, key1, value1)
                // );

                if(StringUtils.isNotEmpty(actualTypeName)) {
                    o = JSONObject.parseArray(
                            argsJson.getString("argInfo"),
                            Class.forName(actualTypeName)
                    );
                } else {
                    o = JSONObject.parseObject(
                            argsJson.getString("argInfo"),
                            argTypeClass, Feature.OrderedField
                    );
                }
            }
            return o;
        } catch (NoSuchMethodException e) {
            log.error("异步重试任务获取【入参方法】信息错误, 方法信息不存在, {}", e.getMessage(), e);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.error("异步重试任务获取【入参类】实例错误, {}", e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            log.error("异步重试任务转换【泛型类型】错误, {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * 实体类型参数组装
     * @param argTypeClass 实体的类信息
     * @param o 实体信息
     * @param key 字段名
     * @param value 字段值
     **/
    public void reflectionArgumentsObject(Class<?> argTypeClass, Object o, String key, Object value) {
        Method method;
        try {
            // 首字符大写的参数名
            String s = StringUtils.capitalize(key);
            // 获取字段信息
            Field field = argTypeClass.getDeclaredField(key);
            // 执行set方法
            method = argTypeClass.getMethod(String.format("set%s", s), field.getType());
            method.setAccessible(true);
            method.invoke(o, value);
        } catch (NoSuchFieldException e) {
            log.error("异步重试任务执行【入参类】SET相关方法错误, 找不到该字段的信息, key:{}, value:{}, NoSuchFieldException错误信息:{}",
                    key, value, e.getMessage());
        } catch (NoSuchMethodException e) {
            log.error("异步重试任务执行【入参类】SET相关方法错误, 找不到SET方法的信息, key:{}, value:{}, NoSuchMethodException错误信息:{}",
                    key, value, e.getMessage());
        } catch (IllegalAccessException e) {
            log.error("异步重试任务执行【入参类】SET相关方法错误, 方法执行权限异常, key:{}, value:{}, IllegalAccessException错误信息:{}",
                    key, value, e.getMessage());
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            log.error("异步重试任务执行【入参类】SET相关方法错误, 执行SET方法发生异常, key:{}, value:{}, InvocationTargetException错误信息:{}",
                    key, value, targetException.getMessage());
        }
    }

}
