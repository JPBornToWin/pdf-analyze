package com.knowmap.top.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.knowmap.top.common.PdfTaskStatus;
import com.knowmap.top.entity.Task;
import com.knowmap.top.mapper.TaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class TaskService {
    @Autowired(required = false)
    private TaskMapper taskMapper;

    // 一次查询5个
    public List<Task> getUndoTasks() {
        QueryWrapper<Task> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("status", PdfTaskStatus.JsonTaskTodo.getCode(), PdfTaskStatus.ContentTaskTodo.getCode()).orderByDesc("id").last("limit 0, 5");
        return taskMapper.selectList(queryWrapper);
    }

    public Task getTaskByDocId(Long docId) {
        QueryWrapper<Task> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("docId", docId);
        return taskMapper.selectOne(queryWrapper);
    }


    public int updateTaskStatus(Task task, int oldStatus) {
       return taskMapper.updateStatus(task.getId(), task.getStatus(), oldStatus);
    }

    public int updateTask(Task task) {
        return taskMapper.updateById(task);
    }

    public List<Task> getNeedSyncTasks(Integer blobTaskStatus, Integer taskStatus) {
        return taskMapper.getTasks(blobTaskStatus, taskStatus);
    }

    public Integer batchUpdateStatus(List<Task> tasks, Integer newStatus) {
        if (Objects.isNull(tasks) || tasks.size() == 0) {
            return 0;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("(");
        tasks.forEach(task -> sb.append(task.getId()).append(","));
        String str = sb.substring(0, sb.length() - 1);

        return taskMapper.batchUpdateStatus(str + ")", newStatus);
    }

}
