package com.knowmap.top.analyze;

import com.knowmap.top.common.PdFBlobStatus;
import com.knowmap.top.common.PdfTaskStatus;
import com.knowmap.top.entity.Task;
import com.knowmap.top.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class SyncTaskHandle extends QuartzJobBean {
    @Autowired
    private TaskService taskService;


    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        log.info("SyncTask");
        // json done
        List<Task> taskJsonDoing = taskService.getNeedSyncTasks(PdFBlobStatus.JsonTaskDone.getCode(), PdfTaskStatus.JsonTaskDoing.getCode());
        taskService.batchUpdateStatus(taskJsonDoing, PdfTaskStatus.JsonTaskDone.getCode());

        // content done
        List<Task> taskContentDoing = taskService.getNeedSyncTasks(PdFBlobStatus.ContentTaskDone.getCode(), PdfTaskStatus.ContentTaskDoing.getCode());
        taskContentDoing.addAll(taskService.getNeedSyncTasks(PdFBlobStatus.ContentTaskDone.getCode(), PdfTaskStatus.JsonTaskDoing.getCode()));

        taskService.batchUpdateStatus(taskContentDoing, PdFBlobStatus.ContentTaskDone.getCode());
    }
}
