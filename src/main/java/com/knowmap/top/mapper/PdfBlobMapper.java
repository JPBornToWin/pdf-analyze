package com.knowmap.top.mapper;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowmap.top.entity.PdfBlob;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
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
    @Update({"update pdf_blob set status = #{status} where id = #{id}"})
    Integer updateStatus(@Param("id") Long id, @Param("baseStatus") Integer baseStatus, @Param("status") Integer status);

    @Select({"select pb.id, pb.status, pb.checksum from pdf_blob pb inner join document d on d.blob_id = pb.id where d.id = #{docId}"})
    PdfBlob getPdfBlob(@Param("docId") Long docId);
}
