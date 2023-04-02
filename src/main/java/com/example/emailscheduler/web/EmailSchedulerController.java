package com.example.emailscheduler.web;

import com.example.emailscheduler.payload.EmailRequest;
import com.example.emailscheduler.payload.EmailResponse;
import com.example.emailscheduler.quartz.job.EmailJob;
import jakarta.validation.Valid;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("schedule/email")
public class EmailSchedulerController {
    Logger logger = LoggerFactory.getLogger(EmailSchedulerController.class);

    @Autowired
    private Scheduler scheduler;


    @PostMapping
    public ResponseEntity<EmailResponse> scheduleEmail(@Valid @RequestBody EmailRequest emailRequest){

        try{
            ZonedDateTime dateTime = ZonedDateTime.of(emailRequest.dateTime(),emailRequest.zoneId());

            if(dateTime.isBefore(ZonedDateTime.now())){
                String error = "dateTime must be after current time.";
                logger.error(error);
                EmailResponse emailResponse =  EmailResponse.failed(error);
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(emailResponse);
            }

            JobDetail jobDetail = buildJobDetail(emailRequest);
            Trigger trigger = buildTrigger(jobDetail,dateTime);
            scheduler.scheduleJob(jobDetail,trigger);

            EmailResponse emailResponse = EmailResponse.success(jobDetail.getKey().getName(),jobDetail.getKey().getGroup());

            return ResponseEntity.ok(emailResponse);

        }catch (SchedulerException e) {
            String error = String.format("Error scheduling email to %s at %s",emailRequest.email(),emailRequest.dateTime());
            logger.error(error,e);
            EmailResponse emailResponse =  EmailResponse.failed(error);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(emailResponse);
        }

    }
    @GetMapping
    public ResponseEntity<String> getApiTest(){
        return ResponseEntity.ok("Get API is working");
    }

    private JobDetail buildJobDetail(EmailRequest scheduleEmailRequest){

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("email",scheduleEmailRequest.email());
        jobDataMap.put("subject",scheduleEmailRequest.subject());
        jobDataMap.put("body",scheduleEmailRequest.body());

        return JobBuilder.newJob(EmailJob.class)
                        .withIdentity(UUID.randomUUID().toString(),"email-jobs")
                        .withDescription("Send Email Job")
                        .usingJobData(jobDataMap)
                        .storeDurably()
                        .build();
    }


private Trigger buildTrigger(JobDetail jobDetail, ZonedDateTime startAt){
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(),"email-triggers")
                .withDescription("Send Email Trigger")
                .startAt(Date.from(startAt.toInstant()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }
}