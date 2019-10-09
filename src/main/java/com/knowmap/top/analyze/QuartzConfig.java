package com.knowmap.top.analyze;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Value("${cronSchedule}")
    private String cronSchedule;


    @Bean
    public JobDetail getBaseTask() {
        return JobBuilder.newJob(TaskHandle.class).withIdentity("baseTask").storeDurably().build();
    }

    @Bean
    public JobDetail getSyncTask() {
        return JobBuilder.newJob(SyncTaskHandle.class).withIdentity("syncTask").storeDurably().build();
    }


    @Bean
    public Trigger getSyncTaskTrigger() {
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronSchedule);
        return TriggerBuilder.newTrigger().forJob(getSyncTask())
                .withIdentity("syncTaskTrigger")
                .withSchedule(scheduleBuilder)
                .startNow()
                .build();
    }


    @Bean
    public Trigger getJsonTaskTrigger() {
//        0 0/1 * * * ? * 每一分钟一次
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronSchedule);
        return TriggerBuilder.newTrigger().forJob(getBaseTask())
                .withIdentity("jsonTaskTrigger")
                .withSchedule(scheduleBuilder)
                .startNow()
                .build();
    }
}
