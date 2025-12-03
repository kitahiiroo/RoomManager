package org.example.roommanager.service;

import org.example.roommanager.entity.Schedule;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleService {

    Schedule createSchedule(Schedule schedule);

    Schedule updateSchedule(Long id, Schedule schedule);

    void deleteSchedule(Long id);

    Schedule getScheduleById(Long id);

    List<Schedule> listAllSchedules();

    List<Schedule> listByClassroomAndDate(Long classroomId, LocalDate date);

    List<Schedule> listByDateAndSection(LocalDate date, Integer startSection, Integer endSection);
}
