package com.knowmap.top.analyze;

import com.knowmap.top.common.PdFBlobStatus;
import com.knowmap.top.entity.PdfBlob;
import com.knowmap.top.entity.Task;
import com.knowmap.top.service.DocumentService;
import com.knowmap.top.service.PdfBlobService;
import com.knowmap.top.service.TaskService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Component
public class HandleTask extends QuartzJobBean {

    @Autowired
    private TaskService taskService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private PdfBlobService pdfBlobService;

    // 调用脚本url常量
    @Value("${pdfDealScript.dealContent}")
    private String pdfContentScript;

    @Value("${pdfDealScript.dealJson")
    private String pdfJsonScript;

    // 状态常量
    @Value("${pdfTaskStatus.jsonTaskTodo}")
    private int jsonTaskTodo;

    @Value("${pdfTaskStatus.jsonTaskDoing}")
    private int jsonTaskDoing;

    @Value(("${pdfTaskStatus.contentTaskTodo}"))
    private int contentTaskTodo;

    @Value(("${pdfTaskStatus.contentTaskDoing}"))
    private int contentTaskDoing;

    private static Logger logger = LoggerFactory.getLogger(QuartzJobBean.class);

    @Transactional
    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            List<Task> tasks = taskService.getUndoTasks();

            tasks.parallelStream().forEach(t -> {

                StringBuilder sb = new StringBuilder();



                if (t.getStatus() == jsonTaskTodo) {

                    Long blobId = documentService.getDocumentById(t.getDocId()).getBlobId();

                    PdfBlob blob = pdfBlobService.getPdfBlobById(blobId);

                    // 已经被处理
                    if (blob.getStatus() == PdFBlobStatus.JsonHadDone.getCode()) {
                        return;
                    }
                    t.setStatus(jsonTaskDoing);

                    Integer oldStatus = blob.getStatus();
                    blob.setStatus(PdFBlobStatus.JsonHadDone.getCode());
                    int ret = pdfBlobService.updatePdfBlobStatus(blob, oldStatus);
                    if (ret == 0)
                        return;

                    if (taskService.updateTaskStatus(t, jsonTaskTodo) > 0) {
                        // todo 调用json的解析script
                        logger.info(t.toString());
                    } else {
                        logger.info("task更新失败");
                        throw new RuntimeException("执行回滚");
                    }

                } else if (t.getStatus() == contentTaskTodo) {
                    // 已经被处理
                    Long blobId = documentService.getDocumentById(t.getDocId()).getBlobId();

                    PdfBlob blob = pdfBlobService.getPdfBlobById(blobId);

                    // 已经被处理
                    if (blob.getStatus() == PdFBlobStatus.ContentHadDone.getCode()) {
                        return;
                    }

                    t.setStatus(contentTaskDoing);

                    Integer oldStatus = blob.getStatus();
                    blob.setStatus(PdFBlobStatus.JsonHadDone.getCode());
                    int ret = pdfBlobService.updatePdfBlobStatus(blob, oldStatus);
                    if (ret == 0)
                        return;

                    // 修改成功则执行
                    if (taskService.updateTaskStatus(t, contentTaskTodo) > 0) {
                        String checksum = blob.getChecksum();

                        // 调用content解析script
                        sb.append(pdfContentScript).
                                append(" ").append(checksum).
                                append(" ").append(t.getId()).
                                append(" ").append(blobId);

                        logger.info(sb.toString());

                        try {
                            Runtime.getRuntime().exec(sb.toString());
                        } catch (IOException e) {
                            logger.error(e.getMessage());
                            throw new RuntimeException(e);
                        }
                    } else {
                        logger.info("task更新失败");
                        throw new RuntimeException("执行回滚");
                    }
                }
            });
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }
}
