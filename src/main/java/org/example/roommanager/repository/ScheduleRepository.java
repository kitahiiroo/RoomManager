package org.example.roommanager.repository;

import org.example.roommanager.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * 排课/占用表 DAO
 */
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    /**
     * 查询某教室在某天的所有占用
     */
    List<Schedule> findByClassroomIdAndDate(Long classroomId, LocalDate date);

    /**
     * 查询某天某节次范围内的所有占用，用来计算空闲教室
     */
    List<Schedule> findByDateAndStartSectionLessThanEqualAndEndSectionGreaterThanEqual(
            LocalDate date,
            Integer endSection,
            Integer startSection
    );

    // 查指定教室、指定日期，且节次有重叠的记录
    List<Schedule> findByClassroomIdAndDateAndStartSectionLessThanEqualAndEndSectionGreaterThanEqual(
            Long classroomId,
            LocalDate date,
            Integer endSection,
            Integer startSection
    );
}
