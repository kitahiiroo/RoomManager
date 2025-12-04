package org.example.roommanager.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ApplicationRequest {
    private Long UserId;
    private Long classroomId;
    private LocalDate date;
    private Integer startSection;
    private Integer endSection;
    private String reason;
    private String applicantName;
    private String courseName;
    private Long courseId;
}