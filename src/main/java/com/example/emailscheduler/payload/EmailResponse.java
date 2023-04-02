package com.example.emailscheduler.payload;

public record EmailResponse(

        boolean success,
        String jobId,
        String jobGroup,
        String message
) {
    public static EmailResponse failed(String message){
        return new EmailResponse(false,"fail","fail",message);
    }
    public static EmailResponse success(String jobId, String jobGroup){
        return new EmailResponse(true,jobId,jobGroup,"Email scheduled successfully.");
    }
}
