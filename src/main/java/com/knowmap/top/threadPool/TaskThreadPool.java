package com.knowmap.top.threadPool;

import com.knowmap.top.common.PdfTaskStatus;
import com.knowmap.top.entity.PdfBlob;
import com.knowmap.top.entity.Task;
import com.knowmap.top.service.PdfBlobService;
import com.knowmap.top.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.sql.Blob;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class TaskThreadPool {
    private static volatile TaskThreadPool INSTANCE;

    private static int MAX = 40;

    // 记录当前线程池的task未处理完毕的数目
    private AtomicInteger currentTaskNum = new AtomicInteger(0);

    public boolean isFull() {
        return currentTaskNum.get() >= MAX;
    }

    public int getCurrentTaskNum() {
        return currentTaskNum.get();
    }


    private volatile ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            20,
            20,
            60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(20),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.DiscardPolicy()
    );

    private TaskThreadPool(){}

    public static TaskThreadPool getInstance() {
        if (INSTANCE == null) {
            synchronized (TaskThreadPool.class) {
                if (INSTANCE == null) {
                    INSTANCE = new TaskThreadPool();
                }
            }
        }

        return INSTANCE;
    }

    public void submitJsonTask(Process process, TaskService taskService, Task task, PdfBlob pdfBlob, PdfBlobService pdfBlobService) {
        currentTaskNum.getAndIncrement();
        threadPoolExecutor.submit(() -> {
            try {
                process.wait(3 * 60 * 1000);
                byte[] bytes;
                try {
                    bytes = StreamUtils.copyToByteArray(process.getErrorStream());
                    if (Objects.nonNull(bytes) && bytes.length > 0) {
                        String errorMsg = new String(bytes);
                        task.setStatus(PdfTaskStatus.TaskExecuteError.getCode());
                        task.setErrorInfo(errorMsg);

                    } else {
                        int oldStatus = task.getStatus();
                        task.setStatus(PdfTaskStatus.JsonTaskDone.getCode());
                        pdfBlobService.updatePdfBlobStatus(pdfBlob.getId(), pdfBlob.getStatus(), PdfTaskStatus.JsonTaskDone.getCode());
                        taskService.updateTaskStatus(task, oldStatus);
                    }
                } catch (IOException e2) {
                    log.error("TaskThreadPool#submitJsonTask #{}", e2);
                }
            } catch (InterruptedException e1) {
                log.error("TaskThreadPool#submitJsonTask #{}", e1);

            } finally {
                currentTaskNum.getAndDecrement();
            }
        });

    }

    public void submitTask(Process process) {
        currentTaskNum.getAndIncrement();
        threadPoolExecutor.submit(() -> {
            try {
                process.wait(3 * 60 * 1000);
            } catch (InterruptedException e) {
                log.error("TaskThreadPool#submitTask #{}", e);
            } finally {
                currentTaskNum.getAndDecrement();
            }
        });
    }

}
