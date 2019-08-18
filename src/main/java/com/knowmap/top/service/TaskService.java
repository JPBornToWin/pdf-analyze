package com.knowmap.top.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.knowmap.top.entity.Task;
import com.knowmap.top.mapper.TaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {
    @Autowired(required = false)
    private TaskMapper taskMapper;

    // 状态常量
    @Value("${pdfTaskStatus.jsonTaskTodo}")
    private int jsonTaskTodo;

    @Value("${pdfTaskStatus.jsonTaskDoing}")
    private int jsonTaskDoing;

    @Value(("${pdfTaskStatus.contentTaskTodo}"))
    private int contentTaskTodo;

    @Value(("${pdfTaskStatus.contentTaskDoing}"))
    private int contentTaskDoing;

    // 一次最多查询5个
    public List<Task> getUndoTasks() {

        QueryWrapper<Task> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("status", jsonTaskTodo, contentTaskTodo).orderByAsc("id").last("limit 0, 1");
        return taskMapper.selectList(queryWrapper);
    }


    public int updateTaskStatus(Task task, int oldStatus) {
        UpdateWrapper<Task> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", task.getId()).eq("status", oldStatus);
        return taskMapper.update(task, updateWrapper);
    }

}
