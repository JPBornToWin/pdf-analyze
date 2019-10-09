package com.knowmap.top.entity;

import lombok.Data;

@Data
public class Task {
    private Long id;

    private Long docId;

    private Integer status;

    private String errorInfo;
}
