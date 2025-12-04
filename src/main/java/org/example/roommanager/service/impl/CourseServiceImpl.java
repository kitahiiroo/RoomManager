package org.example.roommanager.service.impl;

import org.example.roommanager.entity.Course;
import org.example.roommanager.repository.CourseRepository;
import org.example.roommanager.service.CourseService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    public CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public Course createCourse(Course course) {
        return courseRepository.save(course);
    }

    @Override
    public Course updateCourse(Long id, Course course) {
        Course existing = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found, id=" + id));

        existing.setCourseName(course.getCourseName());
        existing.setTeacherName(course.getTeacherName());
        existing.setClassName(course.getClassName());

        return courseRepository.save(existing);
    }

    @Override
    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }

    @Override
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found, id=" + id));
    }

    @Override
    public List<Course> listAllCourses() {
        return courseRepository.findAll();
    }

    @Override
    public List<Course> listByTeacher(String teacherName) {
        return courseRepository.findByTeacherName(teacherName);
    }

    @Override
    public List<Course> listByClassName(String className) {
        return courseRepository.findByClassName(className);
    }


}
