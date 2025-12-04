package org.example.roommanager.controller;

import org.example.roommanager.auth.LoginUserContext;
import org.example.roommanager.entity.Course;
import org.example.roommanager.entity.Schedule;
import org.example.roommanager.repository.CourseRepository;
import org.example.roommanager.repository.ScheduleRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.example.roommanager.entity.Classroom;
import org.example.roommanager.service.ClassroomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.roommanager.dto.ClassroomScheduleResponse;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 教室相关的 REST 接口
 * 路径前缀 /api/classrooms
 */
@RestController
@RequestMapping("/api/classrooms")
public class ClassroomController {

    private final ClassroomService classroomService;
    private final ScheduleRepository scheduleRepository;
    private final CourseRepository courseRepository;

    public ClassroomController(ClassroomService classroomService,
                               ScheduleRepository scheduleRepository, CourseRepository courseRepository) {
        this.classroomService = classroomService;
        this.scheduleRepository = scheduleRepository;
        this.courseRepository = courseRepository;
    }

    @GetMapping("/available")
    public List<Classroom> listAvailable() {
        return classroomService.listAvailableClassrooms();
    }

    @GetMapping("/free")
    public ResponseEntity<List<Classroom>> findFreeClassrooms(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Integer startSection,
            @RequestParam Integer endSection,
            @RequestParam(required = false) Integer minCapacity
    ) {
        List<Classroom> freeList = classroomService.findFreeClassrooms(date, startSection, endSection, minCapacity);
        return ResponseEntity.ok(freeList);
    }

    /**
     * 新增教室
     * POST /api/classrooms
     */
    @PostMapping
    public ResponseEntity<Classroom> create(@RequestBody Classroom classroom) {
        var admin = LoginUserContext.requireAdmin();
        Classroom saved = classroomService.createClassroom(classroom);
        return ResponseEntity.ok(saved);
    }

    /**
     * 更新教室
     * PUT /api/classrooms/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Classroom> update(@PathVariable Long id, @RequestBody Classroom classroom) {
        var admin = LoginUserContext.requireAdmin();
        Classroom updated = classroomService.updateClassroom(id, classroom);
        return ResponseEntity.ok(updated);
    }

    /**
     * 删除教室
     * DELETE /api/classrooms/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        var admin = LoginUserContext.requireAdmin();
        classroomService.deleteClassroom(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 根据ID查询教室
     * GET /api/classrooms/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Classroom> getById(@PathVariable Long id) {
        Classroom classroom = classroomService.getClassroomById(id);
        return ResponseEntity.ok(classroom);
    }

    /**
     * 查询所有教室
     * GET /api/classrooms
     * 可选参数 building / minCapacity / onlyAvailable 进行过滤
     */
    @GetMapping
    public ResponseEntity<List<Classroom>> list(
            @RequestParam(required = false) String building,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false, defaultValue = "false") Boolean onlyAvailable
    ) {
        List<Classroom> result;

        if (building != null) {
            result = classroomService.listByBuilding(building);
        } else if (minCapacity != null) {
            result = classroomService.listByMinCapacity(minCapacity);
        } else if (Boolean.TRUE.equals(onlyAvailable)) {
            result = classroomService.listAvailableClassrooms();
        } else {
            result = classroomService.listAllClassrooms();
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/schedule")
    public ClassroomScheduleResponse getSchedule(
            @PathVariable("id") Long classroomId,
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        // 1. 获取教室信息
        Classroom classroom = classroomService.getClassroomById(classroomId);

        // 2. 计算 date 所在周的周一和周日（周一为一周开始）
        LocalDate baseDate = date != null ? date : LocalDate.now();
        DayOfWeek dow = baseDate.getDayOfWeek(); // MONDAY=1 ... SUNDAY=7
        int diffToMonday = dow.getValue() - DayOfWeek.MONDAY.getValue();
        LocalDate weekStart = baseDate.minusDays(diffToMonday);
        LocalDate weekEnd = weekStart.plusDays(6);

        // 3. 查询这一周内这个教室的排课记录
        List<Schedule> schedules = scheduleRepository
                .findByClassroomIdAndDateBetween(classroomId, weekStart, weekEnd);

        // 4. 组装返回数据
        ClassroomScheduleResponse resp = new ClassroomScheduleResponse();
        resp.setClassroomId(classroomId);
        resp.setClassroomName(classroom.getBuilding() + "-" + classroom.getRoomNumber());
        resp.setWeekStart(weekStart);
        resp.setWeekEnd(weekEnd);
        // week 字段你可以不再使用，也可以按学期周次再算，现在先留空
        resp.setWeek(null);

        List<ClassroomScheduleResponse.Item> items = schedules.stream()
                .map(s -> {
                    ClassroomScheduleResponse.Item it = new ClassroomScheduleResponse.Item();
                    it.setId(s.getId());
                    it.setDayOfWeek(s.getWeekDay()); // 1-7
                    it.setStartSection(s.getStartSection());
                    it.setEndSection(s.getEndSection());
                    it.setDate(s.getDate());
                    // 如果你以后给 Schedule 加上 courseName/teacher/reason 字段，这里再赋值
                    it.setCourseName(s.getCourseName());
                    it.setTeacher(s.getTeacherName());
                    it.setReason(s.getReason());
                    return it;
                })
                .collect(Collectors.toList());

        resp.setItems(items);
        return resp;
    }
}
