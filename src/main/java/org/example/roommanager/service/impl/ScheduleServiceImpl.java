package org.example.roommanager.service.impl;

import org.example.roommanager.entity.Schedule;
import org.example.roommanager.repository.ScheduleRepository;
import org.example.roommanager.service.ScheduleService;
import org.springframework.stereotype.Service;
import org.example.roommanager.exception.BusinessException;
import java.time.LocalDate;
import java.util.List;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;

    public ScheduleServiceImpl(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }


    @Override
    public Schedule createSchedule(Schedule schedule) {
        checkConflict(schedule, null);
        return scheduleRepository.save(schedule);
    }

    @Override
    public Schedule updateSchedule(Long id, Schedule schedule) {
        Schedule existing = scheduleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("SCHEDULE_NOT_FOUND", "排课记录不存在，id=" + id));

        // 先把新值临时设置到 existing 上，复用同一个冲突检测逻辑
        existing.setClassroomId(schedule.getClassroomId());
        existing.setCourseId(schedule.getCourseId());
        existing.setDate(schedule.getDate());
        existing.setWeekDay(schedule.getWeekDay());
        existing.setStartSection(schedule.getStartSection());
        existing.setEndSection(schedule.getEndSection());

        checkConflict(existing, id);

        return scheduleRepository.save(existing);
    }

    @Override
    public void deleteSchedule(Long id) {
        scheduleRepository.deleteById(id);
    }

    @Override
    public Schedule getScheduleById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found, id=" + id));
    }

    @Override
    public List<Schedule> listAllSchedules() {
        return scheduleRepository.findAll();
    }

    @Override
    public List<Schedule> listByClassroomAndDate(Long classroomId, LocalDate date) {
        return scheduleRepository.findByClassroomIdAndDate(classroomId, date);
    }

    @Override
    public List<Schedule> listByDateAndSection(LocalDate date, Integer startSection, Integer endSection) {
        // ScheduleRepository 中的方法名是:
        // findByDateAndStartSectionLessThanEqualAndEndSectionGreaterThanEqual(date, endSection, startSection)
        return scheduleRepository.findByDateAndStartSectionLessThanEqualAndEndSectionGreaterThanEqual(
                date,
                endSection,
                startSection
        );
    }

    /**
     * 检查同一教室、同一天、节次范围是否有冲突
     * @param schedule 新的排课数据
     * @param excludeId 更新时排除自身（新增时传 null）
     */
    private void checkConflict(Schedule schedule, Long excludeId) {
        if (schedule.getClassroomId() == null ||
                schedule.getDate() == null ||
                schedule.getStartSection() == null ||
                schedule.getEndSection() == null) {
            throw new BusinessException("INVALID_PARAM", "排课信息不完整");
        }
        if (schedule.getStartSection() > schedule.getEndSection()) {
            throw new BusinessException("INVALID_SECTION", "开始节次不能大于结束节次");
        }

        var list = scheduleRepository
                .findByClassroomIdAndDateAndStartSectionLessThanEqualAndEndSectionGreaterThanEqual(
                        schedule.getClassroomId(),
                        schedule.getDate(),
                        schedule.getEndSection(),
                        schedule.getStartSection()
                );

        boolean hasConflict = list.stream()
                .anyMatch(s -> excludeId == null || !s.getId().equals(excludeId));

        if (hasConflict) {
            throw new BusinessException("SCHEDULE_CONFLICT",
                    "该教室在该时间段已有课程，请选择其他时间或教室");
        }
    }
}
