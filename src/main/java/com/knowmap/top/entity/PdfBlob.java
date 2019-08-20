package com.knowmap.top.entity;

import lombok.Data;

@Data
public class PdfBlob {
    private Long id;

    private String checksum;

    private Integer status;
}
