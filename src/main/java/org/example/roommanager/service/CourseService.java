package org.example.roommanager.service;

import org.example.roommanager.entity.Course;

import java.util.List;

public interface CourseService {

    Course createCourse(Course course);

    Course updateCourse(Long id, Course course);

    void deleteCourse(Long id);

    Course getCourseById(Long id);

    List<Course> listAllCourses();

    List<Course> listByTeacher(String teacherName);

    List<Course> listByClassName(String className);
}
