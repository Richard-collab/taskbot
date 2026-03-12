package org.example.utils;

import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Date;
import java.util.UUID;

public class QuartzUtils {

    private static final String STR_RUNNABLE = "runnable";
    private static final Scheduler scheduler;

    static {
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    public static Date scheduleJob(Runnable runnable, String cronExpression) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String jobName = "myJob" + uuid;
        String triggerName = "myTrigger" + uuid;
        String groupName = "myGroup" + uuid;
        return scheduleJob(runnable, cronExpression, jobName, triggerName, groupName);
    }
    @SneakyThrows
    public static Date scheduleJob(
            Runnable runnable, String cronExpression, String jobName, String triggerName, String groupName) {
        JobDataMap jobDataMap = new JobDataMap(ImmutableMap.of(STR_RUNNABLE, runnable));
        JobDetail job = JobBuilder.newJob()
                .ofType(MyJob.class)
                .withIdentity(jobName, groupName)
                .usingJobData(jobDataMap)
                .build();
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerName, groupName)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();
        return scheduler.scheduleJob(job, trigger);
    }

    public static class MyJob implements Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            Runnable runnable = (Runnable) context.getJobDetail().getJobDataMap().get("runnable");
            runnable.run();
        }
    }

    public static void main(String[] args) throws SchedulerException {
        Runnable runnable1 = () -> System.out.println("1: " + DatetimeUtils.getStrDatetime(new Date()));
        Runnable runnable2 = () -> System.out.println("2: " + DatetimeUtils.getStrDatetime(new Date()));
        scheduleJob(runnable1,"0 55 15 * * ?");
        scheduleJob(runnable2, "0 56 15 * * ?");
    }
}
