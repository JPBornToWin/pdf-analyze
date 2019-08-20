package com.knowmap.top.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.knowmap.top.common.PdFBlobStatus;
import com.knowmap.top.entity.PdfBlob;
import com.knowmap.top.mapper.PdfBlobMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PdfBlobService {
    @Autowired(required = false)
    private PdfBlobMapper pdfBlobMapper;

    public PdfBlob getPdfBlobById(Long id) {
        QueryWrapper<PdfBlob> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        return pdfBlobMapper.selectOne(queryWrapper);
    }

    public List<PdfBlob> getTodoPdfBlob() {
        QueryWrapper<PdfBlob> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("status", PdFBlobStatus.UnDo.getCode(), PdFBlobStatus.ContentHadDone.getCode()).orderByAsc("id").last("limit 0, 1");
        return pdfBlobMapper.selectList(queryWrapper);
    }

    public Integer updatePdfBlobStatus(PdfBlob pdfBlob, Integer oldStatus) {
        UpdateWrapper<PdfBlob> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("status", oldStatus).eq("id", pdfBlob.getId());
        return pdfBlobMapper.update(pdfBlob, updateWrapper);
    }
}
