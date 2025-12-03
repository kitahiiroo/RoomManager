package org.example.roommanager.repository;

import org.example.roommanager.entity.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 教室表 DAO
 */
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    /**
     * 按楼栋查询教室
     */
    List<Classroom> findByBuilding(String building);

    /**
     * 按状态查询教室，例如 "AVAILABLE"
     */
    List<Classroom> findByStatus(String status);

    /**
     * 按容量大于等于某值查询
     */
    List<Classroom> findByCapacityGreaterThanEqual(Integer capacity);
}