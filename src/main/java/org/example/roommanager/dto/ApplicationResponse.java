package org.example.roommanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {
    private Long id;
    private Long userId;
    private String username;
    private Long classroomId;
    private String classroomName; // 例如 "A栋-101"
    private LocalDate date;
    private Integer startSection;
    private Integer endSection;
    private String reason;
    private String status;
    private LocalDateTime createTime;
}