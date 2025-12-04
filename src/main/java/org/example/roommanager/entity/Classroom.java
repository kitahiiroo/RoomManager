package org.example.roommanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 教室实体，对应数据库表 classroom
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "classroom")
public class Classroom implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 楼栋名称，如 "A栋"
     */
    @Column(nullable = false, length = 50)
    private String building;

    /**
     * 教室号，如 "101"
     */
    @Column(nullable = false, length = 20)
    private String roomNumber;

    /**
     * 最大容量
     */
    @Column(nullable = false)
    private Integer capacity;

    /**
     * 是否有投影仪
     */
    @Column(nullable = false)
    private Boolean hasProjector;

    /**
     * 是否有多媒体/电脑
     */
    @Column(nullable = false)
    private Boolean hasComputer;

    /**
     * 状态：AVAILABLE（可用）、MAINTENANCE（维护中）
     */
    @Column(nullable = false, length = 20)
    private String status;
}
