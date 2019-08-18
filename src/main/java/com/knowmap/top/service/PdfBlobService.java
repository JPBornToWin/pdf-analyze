package com.knowmap.top.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.knowmap.top.entity.PdfBlob;
import com.knowmap.top.mapper.PdfBlobMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PdfBlobService {
    @Autowired(required = false)
    private PdfBlobMapper pdfBlobMapper;

    public String getChecksum(Long id) {
        QueryWrapper<PdfBlob> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        return pdfBlobMapper.selectOne(queryWrapper).getChecksum();
    }
}
