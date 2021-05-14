package cn.locusc.retry.task.controller;

import cn.locusc.retry.task.service.RetryHandlerService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author jayChan
 * 重试任务控制层
 * 2021/4/15
 **/
@RestController
@RequestMapping("/retry/task")
public class RetryHandlerController {

    @Resource
    private RetryHandlerService retryHandlerService;

    @RequestMapping("/executorAsyncRetry")
    public void executorAsyncRetry() {
        retryHandlerService.executorAsyncRetry();
    }

}
