package org.example.roommanager.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Data
public class ClassroomScheduleResponse {

    private Long classroomId;
    private String classroomName;

    // 使用自然周：本周周一、周日
    private LocalDate weekStart;
    private LocalDate weekEnd;

    private Integer week; // 目前只是前端传来的 week 参数，暂时不参与计算

    private List<Item> items;

    // getters/setters
    @Data
    public static class Item {
        private Long id;
        private Integer dayOfWeek;     // 1-7，对应 weekDay
        private Integer startSection;
        private Integer endSection;

        // 以下信息目前 Schedule 表里没有，可以根据课程表扩展
        private String courseName;
        private String reason;
        private String teacher;

        private LocalDate date;

    }
}