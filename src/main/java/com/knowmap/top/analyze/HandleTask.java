package com.knowmap.top.analyze;

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

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            List<Task> tasks = taskService.getUndoTasks();
            tasks.parallelStream().forEach(t -> {

                StringBuilder sb = new StringBuilder();

                if (t.getStatus() == jsonTaskTodo) {
                    t.setStatus(jsonTaskDoing);
                    if (taskService.updateTaskStatus(t, jsonTaskTodo) > 0) {
                        // todo 调用json的解析script
                        logger.info(t.toString());
                    }

                } else if (t.getStatus() == contentTaskTodo) {
                    t.setStatus(contentTaskDoing);

                    // 修改成功则执行
                    if (taskService.updateTaskStatus(t, contentTaskTodo) > 0) {
                        Long blobId = documentService.getBlobId(t.getDocId());
                        String checksum = pdfBlobService.getChecksum(blobId);

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
                        }
                    } // if
                }

            });
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }
}
