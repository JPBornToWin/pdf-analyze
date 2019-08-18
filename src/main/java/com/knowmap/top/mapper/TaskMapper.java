package com.knowmap.top.mapper;


import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowmap.top.entity.Task;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@TableName("task")
public interface TaskMapper extends BaseMapper<Task>{
}
