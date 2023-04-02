package com.example.emailscheduler.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.time.ZoneId;

public record EmailRequest(
        @Email
        @NotEmpty
        String email,

        @NotEmpty
        String subject,

        @NotEmpty
        String body,

        @NotNull
        LocalDateTime dateTime,

        @NotNull
        ZoneId zoneId
) {
}
