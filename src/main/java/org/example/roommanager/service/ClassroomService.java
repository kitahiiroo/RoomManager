package org.example.roommanager.service;

import org.example.roommanager.entity.Classroom;

import java.util.List;

import java.time.LocalDate;

public interface ClassroomService {

    Classroom createClassroom(Classroom classroom);

    Classroom updateClassroom(Long id, Classroom classroom);

    void deleteClassroom(Long id);

    Classroom getClassroomById(Long id);

    List<Classroom> listAllClassrooms();

    List<Classroom> listAvailableClassrooms();

    List<Classroom> listByBuilding(String building);

    List<Classroom> listByMinCapacity(Integer minCapacity);
    /**
     * 根据日期和节次查询空闲教室
     * @param date 日期
     * @param startSection 开始节次
     * @param endSection 结束节次
     * @param minCapacity 最小容量（可选，可为 null）
     * @return 满足条件的空闲教室列表
     */
    List<Classroom> findFreeClassrooms(LocalDate date, Integer startSection, Integer endSection, Integer minCapacity);
}