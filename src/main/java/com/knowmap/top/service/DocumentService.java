package com.knowmap.top.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.knowmap.top.entity.Document;
import com.knowmap.top.mapper.DocumentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DocumentService {
    @Autowired(required = false)
    private DocumentMapper documentMapper;

    public Document getDocumentById(long id) {
        QueryWrapper<Document> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        return documentMapper.selectOne(queryWrapper);
    }

}
