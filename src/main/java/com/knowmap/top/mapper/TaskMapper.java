package com.knowmap.top.mapper;


import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowmap.top.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
@TableName("task")
public interface TaskMapper extends BaseMapper<Task>{

    @Select({"select t.id, t.status, t.retry_times from task t inner join (select d.id from document d inner  join pdf_blob pb on d.blob_id = pb.id where pb.status = #{blobStatus}) tmp on tmp.id = t.doc_id where t.status = #{taskStatus}"})
    List<Task> getTasks(@Param("blobStatus") Integer blobStatus, @Param("taskStatus") Integer taskStatus);

    @Update({"update task set status = #{newStatus} where id in ${str}"})
    Integer batchUpdateStatus(@Param("str") String str, @Param("newStatus") Integer newStatus);
}
