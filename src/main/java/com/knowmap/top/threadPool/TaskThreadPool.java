package com.knowmap.top.threadPool;

import com.knowmap.top.common.PdfTaskStatus;
import com.knowmap.top.entity.PdfBlob;
import com.knowmap.top.entity.Task;
import com.knowmap.top.service.PdfBlobService;
import com.knowmap.top.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class TaskThreadPool {

    static Set<BufferedReader> set = new ConcurrentSkipListSet<>();

    @Autowired
    private TaskService taskService;

    @Autowired
    private PdfBlobService pdfBlobService;

    // 最多允许堆积任务数目
    private static int MAX = 20;

    // 检测脚本程序运行间隔的时间（秒）
    private static int SLEEP_TIME = 30;

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


    public void submitJsonTask(String command, Task task, PdfBlob pdfBlob) {
        System.out.println("command : " + command);
        currentTaskNum.getAndIncrement();
        threadPoolExecutor.submit(() -> {
            Process process = null;
            try {
                process = Runtime.getRuntime().exec(command);
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), Charset.forName("utf-8")));
                boolean done;
                // 监测shell执行
                done = complete(reader);

                if (!done) {
                    log.info("over time or throw exception");
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

                    log.info("thread name : " + Thread.currentThread().getName() + "执行成功");
                }

            } catch (InterruptedException e1) {
                System.out.println(e1);
                log.error("TaskThreadPool#submitJsonTask #{}", e1);

            } catch (IOException e2) {
                System.out.println(e2);
                log.error("TaskThreadPool#submitJsonTask #{}", e2);
            } finally {
                currentTaskNum.getAndDecrement();
                log.info("finally");

                // 终止进程
                if (process != null && process.isAlive()) {
                    process.destroyForcibly();
                }
            }
        });

    }

    public void submitContentTask(Task task, String command, PdfBlob pdfBlob) {
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
                        boolean done;
                        // 监测脚本执行
                        done = complete(reader);

                        if (done) {
                            log.info("done");
                            // 执行成功
                            int oldStatus = task.getStatus();
                            task.setStatus(PdfTaskStatus.ContentTaskDone.getCode());
                            pdfBlobService.updatePdfBlobStatus(pdfBlob.getId(), pdfBlob.getStatus(), PdfTaskStatus.ContentTaskDone.getCode());
                            taskService.updateTaskStatus(task, oldStatus);
                            log.info("thread name : " + Thread.currentThread().getName() + "执行成功");
                        } else if (!errorReader.ready()) {
                            int oldStatus = task.getStatus();
                            task.setStatus(PdfTaskStatus.ContentTaskTodo.getCode());
                            task.setRetryTimes(task.getRetryTimes() == null ? 1 : task.getRetryTimes() + 1);
                            taskService.updateTaskStatus(task, oldStatus);
                            pdfBlobService.updatePdfBlobStatus(pdfBlob.getId(), pdfBlob.getStatus(), PdfTaskStatus.ContentTaskTodo.getCode());
                        } else {
                            String errorMsg = errorReader.readLine();
                            int oldStatus = task.getStatus();
                            task.setStatus(PdfTaskStatus.TaskExecuteError.getCode());
                            task.setErrorInfo(errorMsg);
                            taskService.updateTaskStatus(task, oldStatus);
                            pdfBlobService.updatePdfBlobStatus(pdfBlob.getId(), pdfBlob.getStatus(), PdfTaskStatus.TaskExecuteError.getCode());
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } catch (IOException e2) {
                    log.error("TaskThreadPool#submitJsonTask #{}", e2);
                }
            } finally {
                currentTaskNum.getAndDecrement();
                log.info("finally");
                if (process != null && process.isAlive()) {
                    process.destroyForcibly();
                }
            }
        });
    }

    public boolean complete(BufferedReader reader) throws InterruptedException, IOException {
        String pre = "";
        boolean done = false;
        while (true) {
            int num = SLEEP_TIME;
            boolean flag = false;
            while (num > 0) {
                Thread.sleep(2000);
                if (reader.ready()) {
                    System.out.println("flag true");
                    flag = true;
                    break;
                }
                num--;
            }
            // 超时
            if (!flag) {
                break;
            }
//            String line;
//            while ((line = reader.readLine()) != null) {
//                if (line.toLowerCase().contains("done")) {
//                    done = true;
//                    break;
//                }
//            }
//
//            if (done) {
//                System.out.println("complete");
//                break;
//            }

            char[] chars = new char[512];
            int len;
            while ((len = reader.read(chars)) > 0) {
                String s = pre + String.valueOf(chars, 0, len);
                if (s.toLowerCase().contains("done")) {
                    done = true;
                    break;
                }

                if (s.length() > 4) {
                    pre = s.substring(s.length() - 4);
                } else {
                    pre = s;
                }
            }

            System.out.println("sleep");

        }

        return done;
    }

}
