package cn.locusc.retry.task.mapper;

import cn.locusc.retry.task.domain.RetryTask;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author jayChan
 * 重试任务mapper
 * 2021/5/14
 **/
@Mapper
public interface RetryTaskMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(RetryTask record);

    int insertSelective(RetryTask record);

    RetryTask selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(RetryTask record);

    int updateByPrimaryKey(RetryTask record);

    /**
     * 查询所有可用的重试任务
     **/
    List<RetryTask> queryRetryTaskList();

    /**
     * 根据实体条件查询重试任务
     **/
    List<RetryTask> queryRetryTaskListByEntity(RetryTask record);
}