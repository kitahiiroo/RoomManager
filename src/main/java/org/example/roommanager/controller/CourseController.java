package org.example.roommanager.controller;

import org.example.roommanager.auth.LoginUserContext;
import org.example.roommanager.entity.Course;
import org.example.roommanager.service.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程相关 REST 接口
 * 路径前缀 /api/courses
 */
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * 新增课程
     * POST /api/courses
     */
    @PostMapping
    public ResponseEntity<Course> create(@RequestBody Course course) {
        var admin = LoginUserContext.requireAdmin();
        Course saved = courseService.createCourse(course);
        return ResponseEntity.ok(saved);
    }

    /**
     * 更新课程
     * PUT /api/courses/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Course> update(@PathVariable Long id, @RequestBody Course course) {
        var admin = LoginUserContext.requireAdmin();
        Course updated = courseService.updateCourse(id, course);
        return ResponseEntity.ok(updated);
    }

    /**
     * 删除课程
     * DELETE /api/courses/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        var admin = LoginUserContext.requireAdmin();
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 根据ID查询课程
     * GET /api/courses/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Course> getById(@PathVariable Long id) {
        Course course = courseService.getCourseById(id);
        return ResponseEntity.ok(course);
    }

    /**
     * 查询课程列表
     * GET /api/courses
     * 可选参数 teacherName / className
     */
    @GetMapping
    public ResponseEntity<List<Course>> list(
            @RequestParam(required = false) String teacherName,
            @RequestParam(required = false) String className
    ) {
        List<Course> result;

        if (teacherName != null) {
            result = courseService.listByTeacher(teacherName);
        } else if (className != null) {
            result = courseService.listByClassName(className);
        } else {
            result = courseService.listAllCourses();
        }

        return ResponseEntity.ok(result);
    }
}