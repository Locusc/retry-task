package cn.locusc.retry.task.annotation;


import java.lang.annotation.*;

/**
 * @author jayChan
 * 重试注解
 * 2021/4/8
 **/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RetryHandler {

    /**
     * 任务名称 不可重复 重复不创建新任务
     **/
    String taskName();

    /**
     * 是否同步重试
     **/
    boolean isSyncRetry() default false;

    /**
     * 0 < 最大重试次数 < 10
     **/
    int maxAttempts() default 3;

    /**
     * 进行重试的错误类型
     **/
    Class<? extends Throwable>[] value();

    /**
     * 最大休眠时间
     **/
    int maxInterval() default 30000;

    /**
     * 乘数
     **/
    int multiplier() default 2;

    /**
     * 初始休眠时间
     **/
    int initialInterval() default 1000;

    /**
     * 判断是否重试成功的规则
     **/
    String rule() default "";

}
