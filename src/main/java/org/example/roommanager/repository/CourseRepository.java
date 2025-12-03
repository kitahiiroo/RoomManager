package org.example.roommanager.repository;

import org.example.roommanager.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 课程表 DAO
 */
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByTeacherName(String teacherName);

    List<Course> findByClassName(String className);
}