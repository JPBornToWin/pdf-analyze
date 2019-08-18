package com.knowmap.top.mapper;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowmap.top.entity.Document;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@TableName("document")
public interface DocumentMapper extends BaseMapper<Document> {
}
