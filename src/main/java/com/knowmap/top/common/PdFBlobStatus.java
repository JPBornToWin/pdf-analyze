package com.knowmap.top.common;

public enum PdFBlobStatus {
    JsonTaskTodo(0),
    JsonTaskDoing(1),
    JsonTaskDone(2),
    TaskExecuteError(4),
    ContentTaskDoing(5),
    ContentTaskDone(6);

    int code;

    PdFBlobStatus(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
