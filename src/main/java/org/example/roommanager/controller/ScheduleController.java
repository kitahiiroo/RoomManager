package org.example.roommanager.controller;

import org.example.roommanager.auth.LoginUserContext;
import org.example.roommanager.dto.ScheduleItemResponse;
import org.example.roommanager.entity.Classroom;
import org.example.roommanager.entity.Schedule;
import org.example.roommanager.repository.ScheduleRepository;
import org.example.roommanager.service.ClassroomService;
import org.example.roommanager.service.ScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 排课/教室占用相关接口
 * 路径前缀 /api/schedules
 */
@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleRepository scheduleRepository;
    private final ClassroomService classroomService;
    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleRepository scheduleRepository,
                              ClassroomService classroomService,
                              ScheduleService scheduleService) {
        this.scheduleRepository = scheduleRepository;
        this.classroomService = classroomService;
        this.scheduleService = scheduleService;
    }

    /**
     * 新增排课记录
     * POST /api/schedules
     */
    @PostMapping
    public ResponseEntity<Schedule> create(@RequestBody Schedule schedule) {
        var admin = LoginUserContext.requireAdmin();
        Schedule saved = scheduleService.createSchedule(schedule);
        return ResponseEntity.ok(saved);
    }

    /**
     * 更新排课记录
     * PUT /api/schedules/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Schedule> update(@PathVariable Long id, @RequestBody Schedule schedule) {
        var admin = LoginUserContext.requireAdmin();
        Schedule updated = scheduleService.updateSchedule(id, schedule);
        return ResponseEntity.ok(updated);
    }

    /**
     * 删除排课记录
     * DELETE /api/schedules/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        var admin = LoginUserContext.requireAdmin();
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
     * 按条件查询排课（返回原始 Schedule 实体）
     * GET /api/schedules/search
     *
     * 可选参数：
     *  - classroomId + date: 查询某教室在某天的排课
     *  - date + startSection + endSection: 查询某天某时间段所有占用
     *  - 都不传：返回全部
     *
     * 注意：这个接口主要给内部使用；前端“占用一览”不要用它。
     */
    @GetMapping("/search")
    public ResponseEntity<List<Schedule>> search(
            @RequestParam(required = false) Long classroomId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer startSection,
            @RequestParam(required = false) Integer endSection
    ) {
        List<Schedule> result;

        if (classroomId != null && date != null) {
            result = scheduleService.listByClassroomAndDate(classroomId, date);
        } else if (date != null && startSection != null && endSection != null) {
            result = scheduleService.listByDateAndSection(date, startSection, endSection);
        } else {
            result = scheduleService.listAllSchedules();
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 占用一览：按日期范围列出所有占用记录，可选按教室过滤。
     *
     * GET /api/schedules?startDate=2025-12-01&endDate=2025-12-07[&classroomId=1]
     *
     * 返回带 classroomName 的 DTO，专门给前端列表用。
     */
    @GetMapping
    public List<ScheduleItemResponse> list(
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "classroomId", required = false) Long classroomId
    ) {

        List<Schedule> schedules;

        // 如果没有日期范围，返回全部（可选按教室过滤）
        if (startDate == null || endDate == null) {
            if (classroomId != null) {
                // 这里简单用 service 的 listByClassroomAndDate 之类会不方便，
                // 我们就直接用 findAll + 过滤，或者你单独加一个按 classroomId 查所有的 Repository 方法。
                schedules = scheduleRepository.findAll().stream()
                        .filter(s -> classroomId.equals(s.getClassroomId()))
                        .sorted((a, b) -> {
                            int cmp = a.getDate().compareTo(b.getDate());
                            if (cmp != 0) return cmp;
                            return a.getStartSection().compareTo(b.getStartSection());
                        })
                        .collect(Collectors.toList());
            } else {
                // 全部排课，按日期+节次排序
                schedules = scheduleRepository.findAll().stream()
                        .sorted((a, b) -> {
                            int cmp = a.getDate().compareTo(b.getDate());
                            if (cmp != 0) return cmp;
                            return a.getStartSection().compareTo(b.getStartSection());
                        })
                        .collect(Collectors.toList());
            }
        } else {
            // 有日期范围时，使用 Between 查询
            if (classroomId != null) {
                schedules = scheduleRepository
                        .findByClassroomIdAndDateBetweenOrderByDateAscStartSectionAsc(
                                classroomId, startDate, endDate);
            } else {
                schedules = scheduleRepository
                        .findByDateBetweenOrderByDateAscStartSectionAsc(startDate, endDate);
            }
        }

        return schedules.stream()
                .map(s -> {
                    Classroom classroom = classroomService.getClassroomById(s.getClassroomId());
                    ScheduleItemResponse r = new ScheduleItemResponse();
                    r.setId(s.getId());
                    r.setClassroomId(s.getClassroomId());
                    r.setClassroomName(
                            classroom.getBuilding() + "-" + classroom.getRoomNumber()
                    );
                    r.setDate(s.getDate());
                    r.setWeekDay(s.getWeekDay());
                    r.setStartSection(s.getStartSection());
                    r.setEndSection(s.getEndSection());
                    r.setCourseName(s.getCourseName());
                    r.setReason(s.getReason());
                    r.setTeacher(s.getTeacherName());
                    return r;
                })
                .collect(Collectors.toList());
    }
}