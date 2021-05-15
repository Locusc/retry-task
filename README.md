<p align="center">
	<strong>方法(接口)重试任务示例-retry-task</strong>
</p>

## 说明:
重试任务通过反射转换参数类型以及实参, Aop拦截错误, 注解设置重试参数,
异步任务通过定时任务调度进行重试, 默认重试次数为三次。

## 使用:
### 1.添加定时任务表到数据库
```
    retry-task\src\main\resources\oracle-retry-task.sql
```

### 2.添加定时任务http调用或者注解调用
```java
    @Resource
    private RetryHandlerService retryHandlerService;

    @RequestMapping("/executorAsyncRetry")
    public void executorAsyncRetry() {
        retryHandlerService.executorAsyncRetry();
    }
```

### 任务参数设置和使用
```java
    // 参考注解RetryHandler.java
    @RetryHandler(
            taskName = "接口重试",
            maxAttempts = 10,
            maxInterval = 10000,
            value = { NullPointerException.class, ArrayIndexOutOfBoundsException.class }
    )
```

## 已完成：
* [x] 异步重试(定时任务调用)

## 未完成：
* [ ] 同步重试(延时队列实现)
* [ ] 入参转换支持复杂数据类型(多层嵌套)
* [ ] 优化相同任务对比(现为入参对比)
