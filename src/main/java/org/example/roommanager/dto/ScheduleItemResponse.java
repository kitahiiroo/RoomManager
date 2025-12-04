package org.example.roommanager.dto;

import lombok.*;

import java.time.LocalDate;

@Data
public class ScheduleItemResponse {

    private Long id;
    private Long classroomId;
    private String classroomName;

    private LocalDate date;
    private Integer weekDay;
    private Integer startSection;
    private Integer endSection;

    private String courseName;
    private String reason;
    private String teacher;

}
