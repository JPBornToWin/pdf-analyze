package com.knowmap.top.threadPool;

import com.knowmap.top.common.PdfTaskStatus;
import com.knowmap.top.entity.PdfBlob;
import com.knowmap.top.entity.Task;
import com.knowmap.top.service.PdfBlobService;
import com.knowmap.top.service.TaskService;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
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

    private TaskThreadPool() {
    }

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

    public void submitJsonTask(String command, TaskService taskService, Task task, PdfBlob pdfBlob, PdfBlobService pdfBlobService) {
        System.out.println("command : " + command);
        currentTaskNum.getAndIncrement();
        threadPoolExecutor.submit(() -> {
            Process process = null;
            try {
                process = Runtime.getRuntime().exec(command);
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), Charset.forName("utf-8")));
                Thread.sleep(5 * 1000);
                StringBuilder currentLine = new StringBuilder();
                while (reader.ready()) {

                    while (reader.ready()) {
                        currentLine.append(reader.readLine());
                    }

                    if (errorReader.ready()) {
                        break;
                    }

                    System.out.println("心跳包: " + currentLine);

                    if (currentLine.toString().contains("done")) {
                        break;
                    }
                    Thread.sleep(5 * 1000);
                }

                if (currentLine == null || !currentLine.toString().contains("done")) {
                    if (errorReader.ready()) {
                        // 执行抛出异常
                        String errorMsg = errorReader.readLine();
                        System.out.println(errorMsg + " 抛出异常");
                        int oldStatus = task.getStatus();
                        task.setStatus(PdfTaskStatus.TaskExecuteError.getCode());
                        task.setErrorInfo(errorMsg);
                        taskService.updateTaskStatus(task, oldStatus);
                        pdfBlobService.updatePdfBlobStatus(pdfBlob.getId(), pdfBlob.getStatus(), PdfTaskStatus.TaskExecuteError.getCode());

                    } else {
                        // 超时
                        int oldStatus = task.getStatus();
                        task.setStatus(PdfTaskStatus.JsonTaskTodo.getCode());
                        task.setRetryTimes(task.getRetryTimes() == null ? 1 : task.getRetryTimes() + 1);
                        taskService.updateTaskStatus(task, oldStatus);
                        pdfBlobService.updatePdfBlobStatus(pdfBlob.getId(), pdfBlob.getStatus(), PdfTaskStatus.JsonTaskTodo.getCode());
                        System.out.println("thread name : " + Thread.currentThread().getName() + " 心跳超时");
                    }
                } else {
                    // 执行成功
                    int oldStatus = task.getStatus();
                    task.setStatus(PdfTaskStatus.JsonTaskDone.getCode());
                    pdfBlobService.updatePdfBlobStatus(pdfBlob.getId(), pdfBlob.getStatus(), PdfTaskStatus.JsonTaskDone.getCode());
                    taskService.updateTaskStatus(task, oldStatus);

                    System.out.println("thread name : " + Thread.currentThread().getName() + "执行成功");
                }

            } catch (InterruptedException e1) {
                log.error("TaskThreadPool#submitJsonTask #{}", e1);

            } catch (IOException e2) {
                log.error("TaskThreadPool#submitJsonTask #{}", e2);
            } finally {
                // 终止进程
                if (process.isAlive()) {
                    process.destroyForcibly();
                }

                currentTaskNum.getAndDecrement();
            }
        });

    }

    public void submitContentTask(TaskService taskService, Task task, String command, PdfBlob pdfBlob, PdfBlobService pdfBlobService) {
        currentTaskNum.getAndIncrement();
        threadPoolExecutor.submit(() -> {
            Process process = null;
            try {
                try {
                    try {
                        log.info(command);
                        process = Runtime.getRuntime().exec(command);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), Charset.forName("utf-8")));
                        Thread.sleep(5 * 1000);
                        StringBuilder currentLine = new StringBuilder();
                        while (reader.ready()) {
                            while (reader.ready()) {
                                currentLine.append(reader.readLine());
                            }

                            if (errorReader.ready()) {
                                break;
                            }

                            System.out.println("心跳包: " + currentLine);
                            if (currentLine.toString().contains("done")) {
                                break;
                            }
                            Thread.sleep(3 * 1000);
                        }

                        // 异常超时
                        if (!currentLine.toString().contains("done") && !errorReader.ready()) {
                            int oldStatus = task.getStatus();
                            task.setStatus(PdfTaskStatus.ContentTaskTodo.getCode());
                            task.setRetryTimes(task.getRetryTimes() == null ? 1 : task.getRetryTimes() + 1);
                            taskService.updateTaskStatus(task, oldStatus);
                            pdfBlobService.updatePdfBlobStatus(pdfBlob.getId(), pdfBlob.getStatus(), PdfTaskStatus.ContentTaskTodo.getCode());
                        }

                        // 异常
                        if (errorReader.ready()) {
                            pdfBlobService.updatePdfBlobStatus(pdfBlob.getId(), pdfBlob.getStatus(), PdfTaskStatus.TaskExecuteError.getCode());
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } catch (IOException e2) {
                    log.error("TaskThreadPool#submitJsonTask #{}", e2);
                }
            } finally {
                if (process.isAlive()) {
                    process.destroyForcibly();
                }

                currentTaskNum.getAndDecrement();
            }
        });
    }

}
