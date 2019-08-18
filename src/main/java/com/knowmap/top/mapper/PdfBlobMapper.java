package com.knowmap.top.mapper;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowmap.top.entity.PdfBlob;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@TableName("pdf_blob")
public interface PdfBlobMapper extends BaseMapper<PdfBlob> {
}
