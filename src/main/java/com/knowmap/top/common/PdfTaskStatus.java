package com.knowmap.top.common;

public enum  PdfTaskStatus {
    JsonTaskTodo(0),
    JsonTaskDoing(1),
    JsonTaskDone(2),
    TaskExecuteError(4),
    ContentTaskTodo(2),
    ContentTaskDoing(5),
    ContentTaskDone(6);

    int code;

    PdfTaskStatus(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
