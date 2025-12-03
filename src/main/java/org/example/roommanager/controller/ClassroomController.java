package org.example.roommanager.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.example.roommanager.entity.Classroom;
import org.example.roommanager.service.ClassroomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 教室相关的 REST 接口
 * 路径前缀 /api/classrooms
 */
@RestController
@RequestMapping("/api/classrooms")
public class ClassroomController {

    private final ClassroomService classroomService;

    public ClassroomController(ClassroomService classroomService) {
        this.classroomService = classroomService;
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
        Classroom saved = classroomService.createClassroom(classroom);
        return ResponseEntity.ok(saved);
    }

    /**
     * 更新教室
     * PUT /api/classrooms/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Classroom> update(@PathVariable Long id, @RequestBody Classroom classroom) {
        Classroom updated = classroomService.updateClassroom(id, classroom);
        return ResponseEntity.ok(updated);
    }

    /**
     * 删除教室
     * DELETE /api/classrooms/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
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
}
