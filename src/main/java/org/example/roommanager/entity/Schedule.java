package org.example.roommanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 排课/占用记录，用来判断某个时间段教室是否空闲
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "schedule")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 教室ID
     */
    @Column(nullable = false)
    private Long classroomId;

    /**
     * 课程ID（如果不想拆表，也可以直接存课程名/教师名）
     */
    private Long courseId;

    /**
     * 日期（可选：也可以只用 weekDay 和周次）
     */
    @Column(nullable = false)
    private LocalDate date;

    /**
     * 周几：1-7
     */
    @Column(nullable = false)
    private Integer weekDay;

    /**
     * 开始节次，例如 1
     */
    @Column(nullable = false)
    private Integer startSection;

    /**
     * 结束节次，例如 2
     */
    @Column(nullable = false)
    private Integer endSection;

    private String reason;

    private String courseName;

    private String teacherName;
}
