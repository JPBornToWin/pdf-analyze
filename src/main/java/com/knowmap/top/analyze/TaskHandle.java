package com.knowmap.top.analyze;

import com.knowmap.top.common.FileConstant;
import com.knowmap.top.common.PdfTaskStatus;
import com.knowmap.top.entity.PdfBlob;
import com.knowmap.top.entity.Task;
import com.knowmap.top.service.DocumentService;
import com.knowmap.top.service.PdfBlobService;
import com.knowmap.top.service.TaskService;
import com.knowmap.top.threadPool.TaskThreadPool;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class TaskHandle extends QuartzJobBean {

    @Autowired
    private TaskService taskService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private PdfBlobService pdfBlobService;

    // 调用脚本url常量
    @Value("${pdfDealScript.dealContent}")
    private String pdfContentScript;

    @Value("${pdfDealScript.dealJson}")
    private String pdfJsonScript;

    @Value("${taskMaxRetryTimes}")
    private Integer taskMaxRetryTimes;


    @Transactional
    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("task");
        try {
//            logger.info("开始啦");
            // 判断线程池的状态
            if (TaskThreadPool.getInstance().isFull()) {
                return;
            }
//            logger.info("开始啦 - 1");

            List<Task> tasks = taskService.getUndoTasks();

            tasks.stream().forEach(t -> {
                // 判断线程池的状态
                if (!TaskThreadPool.getInstance().isFull() || t.getRetryTimes() > taskMaxRetryTimes) {

                    StringBuilder sb = new StringBuilder();

                    if (t.getStatus().intValue() == PdfTaskStatus.JsonTaskTodo.getCode()) {

                        PdfBlob pdfBlob = pdfBlobService.getLock(t.getId());

                        // 获取到blob锁
                        if (Objects.nonNull(pdfBlob) && (pdfBlob.getStatus().intValue() == PdfTaskStatus.JsonTaskTodo.getCode().intValue() || pdfBlob.getStatus().intValue() == PdfTaskStatus.TaskExecuteError.getCode().intValue())) {
                            // 更新 blob'status
                            pdfBlobService.updatePdfBlobStatus(pdfBlob.getId(), pdfBlob.getStatus(), PdfTaskStatus.JsonTaskDoing.getCode());

                            t.setStatus(PdfTaskStatus.JsonTaskDoing.getCode());
                            // 更新 task'status
                            if (taskService.updateTaskStatus(t, PdfTaskStatus.JsonTaskTodo.getCode()) > 0) {
                                String checksum = pdfBlob.getChecksum();
                                String pdfPath = FileConstant.getPdfPath(checksum);
                                String jsonDir = FileConstant.getJsonDir(checksum);
                                File file = new File(jsonDir);
                                if (!file.exists()) {
                                    file.mkdirs();
                                }
                                sb.append(pdfJsonScript).
                                        append(" ").
                                        append(pdfPath).
                                        append(" ").
                                        append(jsonDir);
                                TaskThreadPool.getInstance().submitJsonTask(sb.toString(), taskService, t, pdfBlob, pdfBlobService);
                            } else {
                                log.debug("task更新失败");
                                throw new RuntimeException("执行回滚");
                            }
                        } else {
                            // 同步task状态
                            if (Objects.nonNull(pdfBlob)) {
                                t.setStatus(pdfBlob.getStatus());
                                taskService.updateTaskStatus(t, PdfTaskStatus.JsonTaskTodo.getCode());
                            }
                        }

                    } else if (t.getStatus().intValue() == PdfTaskStatus.ContentTaskTodo.getCode()) {

                        PdfBlob pdfBlob = pdfBlobService.getLock(t.getId());

                        // 已经被处理
                        if (Objects.nonNull(pdfBlob) && (pdfBlob.getStatus().intValue() == PdfTaskStatus.ContentTaskTodo.getCode().intValue() || pdfBlob.getStatus().intValue() == PdfTaskStatus.TaskExecuteError.getCode().intValue())) {
                            // 更新 task'status
                            pdfBlobService.updatePdfBlobStatus(pdfBlob.getId(), pdfBlob.getStatus(), PdfTaskStatus.ContentTaskDoing.getCode());
                            t.setStatus(PdfTaskStatus.ContentTaskTodo.getCode());
                            // 更新 task'status
                            if (taskService.updateTaskStatus(t, PdfTaskStatus.ContentTaskTodo.getCode()) > 0) {
                                // 调用content解析script
                                t.setRetryTimes(t.getRetryTimes() + 1);
                                sb.append(pdfContentScript).
                                        append(" ").append(pdfBlob.getChecksum()).
                                        append(" ").append(t.getId()).
                                        append(" ").append(pdfBlob.getId());

                                // 此处调用线程池
                                TaskThreadPool.getInstance().submitContentTask(sb.toString(), pdfBlob, pdfBlobService);
                            } else {
                                log.debug("task更新失败");
                                throw new RuntimeException("执行回滚");
                            }

                        } else {
                            // 同步task状态
                            if (Objects.nonNull(pdfBlob)) {
                                t.setStatus(pdfBlob.getStatus());
                                taskService.updateTaskStatus(t, PdfTaskStatus.ContentTaskTodo.getCode());
                            }
                        }
                    }
                }

            });
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }
}
