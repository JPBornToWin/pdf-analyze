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
    public JobDetail getTask() {
        return JobBuilder.newJob(HandleTask.class).withIdentity("handleJsonTask").storeDurably().build();
    }


    @Bean
    public Trigger getJsonTaskTrigger() {
//        0 0/1 * * * ? * 每一分钟一次
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronSchedule);
        return TriggerBuilder.newTrigger().forJob(getTask())
                .withIdentity("jsonTaskTrigger")
                .withSchedule(scheduleBuilder)
                .startNow()
                .build();
    }
}
