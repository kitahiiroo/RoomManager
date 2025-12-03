package org.example.roommanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课程信息，对应 course 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 课程名称，例如 "高等数学"
     */
    @Column(nullable = false, length = 100)
    private String courseName;

    /**
     * 任课教师姓名
     */
    @Column(nullable = false, length = 50)
    private String teacherName;

    /**
     * 上课班级，例如 "计科2201"
     */
    @Column(nullable = false, length = 50)
    private String className;
}