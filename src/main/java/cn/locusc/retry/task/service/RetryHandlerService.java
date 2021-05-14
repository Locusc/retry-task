package cn.locusc.retry.task.service;

/**
 * @author jayChan
 * 重试任务
 * 2021/4/15
 **/
public interface RetryHandlerService {

    /**
     * 异步重试方法
     **/
    void executorAsyncRetry();

}
