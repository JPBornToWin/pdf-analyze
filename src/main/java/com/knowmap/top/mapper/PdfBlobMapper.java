package com.knowmap.top.mapper;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowmap.top.entity.PdfBlob;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
@TableName("pdf_blob")
public interface PdfBlobMapper extends BaseMapper<PdfBlob> {

    /**
     *
     * @param id PdfBlob 的 id
     * @param baseStatus 原始 status 的值
     * @param status 将要被修改 status 的值
     * @return
     */
    @Update({"update pdf_blob set status = #{2} where id = #{0} and status = #{1}"})
    Integer updateStatus(Long id, Integer baseStatus, Integer status);
}
