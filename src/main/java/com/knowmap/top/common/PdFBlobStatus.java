package com.knowmap.top.common;

public enum PdFBlobStatus {
    UnDo(0),

    JsonHadDone(1),

    ContentHadDone(2);

    int code;

    PdFBlobStatus(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
