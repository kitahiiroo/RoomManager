package org.example.roommanager.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "room_application")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "classroom_id", nullable = false)
    private Long classroomId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "start_section", nullable = false)
    private Integer startSection;

    @Column(name = "end_section", nullable = false)
    private Integer endSection;

    @Column(length = 255)
    private String reason;

    @Column(nullable = false, length = 20)
    private String status; // PENDING / APPROVED / REJECTED

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    // 新增：申请人姓名
    private String applicantName;

    // 新增：课程名称
    private String courseName;

    private Long courseId;
}