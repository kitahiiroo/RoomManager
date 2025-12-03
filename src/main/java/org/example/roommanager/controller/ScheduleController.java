package org.example.roommanager.controller;

import org.example.roommanager.entity.Schedule;
import org.example.roommanager.service.ScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 排课/教室占用相关接口
 * 路径前缀 /api/schedules
 */
@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /**
     * 新增排课记录
     * POST /api/schedules
     */
    @PostMapping
    public ResponseEntity<Schedule> create(@RequestBody Schedule schedule) {
        Schedule saved = scheduleService.createSchedule(schedule);
        return ResponseEntity.ok(saved);
    }

    /**
     * 更新排课记录
     * PUT /api/schedules/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Schedule> update(@PathVariable Long id, @RequestBody Schedule schedule) {
        Schedule updated = scheduleService.updateSchedule(id, schedule);
        return ResponseEntity.ok(updated);
    }

    /**
     * 删除排课记录
     * DELETE /api/schedules/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 根据ID查询排课记录
     * GET /api/schedules/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Schedule> getById(@PathVariable Long id) {
        Schedule schedule = scheduleService.getScheduleById(id);
        return ResponseEntity.ok(schedule);
    }

    /**
     * 查询排课列表
     * GET /api/schedules
     * 可选参数：classroomId + date 或 date + startSection + endSection
     */
    @GetMapping
    public ResponseEntity<List<Schedule>> list(
            @RequestParam(required = false) Long classroomId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer startSection,
            @RequestParam(required = false) Integer endSection
    ) {
        List<Schedule> result;

        // 优先走 "某教室在某天的所有排课"
        if (classroomId != null && date != null) {
            result = scheduleService.listByClassroomAndDate(classroomId, date);
        }
        // 否则，如果提供了日期和节次范围，查这段时间所有占用
        else if (date != null && startSection != null && endSection != null) {
            result = scheduleService.listByDateAndSection(date, startSection, endSection);
        }
        // 否则返回全表
        else {
            result = scheduleService.listAllSchedules();
        }

        return ResponseEntity.ok(result);
    }
}