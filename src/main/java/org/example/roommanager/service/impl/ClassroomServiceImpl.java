package org.example.roommanager.service.impl;

import org.example.roommanager.entity.Classroom;
import org.example.roommanager.entity.Schedule;
import org.example.roommanager.repository.ClassroomRepository;
import org.example.roommanager.repository.ScheduleRepository;
import org.example.roommanager.service.ClassroomService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ClassroomServiceImpl implements ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final ScheduleRepository scheduleRepository;

    public ClassroomServiceImpl(ClassroomRepository classroomRepository,
                                ScheduleRepository scheduleRepository) {
        this.classroomRepository = classroomRepository;
        this.scheduleRepository = scheduleRepository;
    }

    @Override
    public List<Classroom> findFreeClassrooms(LocalDate date,
                                              Integer startSection,
                                              Integer endSection,
                                              Integer minCapacity) {
        if (date == null || startSection == null || endSection == null) {
            throw new IllegalArgumentException("date, startSection, endSection cannot be null");
        }
        if (startSection > endSection) {
            throw new IllegalArgumentException("startSection cannot be greater than endSection");
        }

        // 1. 查询这一天在这一节次范围内所有的占用记录
        // 使用 ScheduleRepository 中的方法：
        // findByDateAndStartSectionLessThanEqualAndEndSectionGreaterThanEqual(date, endSection, startSection)
        List<Schedule> occupiedList =
                scheduleRepository.findByDateAndStartSectionLessThanEqualAndEndSectionGreaterThanEqual(
                        date,
                        endSection,
                        startSection
                );

        // 2. 提取所有已占用教室的 ID 集合
        Set<Long> occupiedClassroomIds = occupiedList.stream()
                .map(Schedule::getClassroomId)
                .collect(Collectors.toSet());

        // 3. 查询所有状态为 AVAILABLE 的教室，按最小容量过滤（如果传了）
        List<Classroom> candidates;
        if (minCapacity != null) {
            candidates = classroomRepository.findByCapacityGreaterThanEqual(minCapacity)
                    .stream()
                    .filter(c -> "AVAILABLE".equalsIgnoreCase(c.getStatus()))
                    .collect(Collectors.toList());
        } else {
            candidates = classroomRepository.findByStatus("AVAILABLE");
        }

        // 4. 从候选列表中剔除已占用的教室 => 得到空闲教室
        return candidates.stream()
                .filter(c -> !occupiedClassroomIds.contains(c.getId()))
                .collect(Collectors.toList());
    }
    @Override
    public Classroom createClassroom(Classroom classroom) {
        // 初始状态设为 AVAILABLE（可用），也可以由前端传
        if (classroom.getStatus() == null) {
            classroom.setStatus("AVAILABLE");
        }
        return classroomRepository.save(classroom);
    }

    @Override
    public Classroom updateClassroom(Long id, Classroom classroom) {
        Optional<Classroom> optional = classroomRepository.findById(id);
        if (optional.isEmpty()) {
            throw new RuntimeException("Classroom not found, id=" + id);
        }
        Classroom existing = optional.get();
        existing.setBuilding(classroom.getBuilding());
        existing.setRoomNumber(classroom.getRoomNumber());
        existing.setCapacity(classroom.getCapacity());
        existing.setHasProjector(classroom.getHasProjector());
        existing.setHasComputer(classroom.getHasComputer());
        existing.setStatus(classroom.getStatus());
        return classroomRepository.save(existing);
    }

    @Override
    public void deleteClassroom(Long id) {
        classroomRepository.deleteById(id);
    }

    @Override
    public Classroom getClassroomById(Long id) {
        return classroomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Classroom not found, id=" + id));
    }

    @Override
    public List<Classroom> listAllClassrooms() {
        return classroomRepository.findAll();
    }

    @Override
    public List<Classroom> listAvailableClassrooms() {
        return classroomRepository.findByStatus("AVAILABLE");
    }

    @Override
    public List<Classroom> listByBuilding(String building) {
        return classroomRepository.findByBuilding(building);
    }

    @Override
    public List<Classroom> listByMinCapacity(Integer minCapacity) {
        return classroomRepository.findByCapacityGreaterThanEqual(minCapacity);
    }
}
