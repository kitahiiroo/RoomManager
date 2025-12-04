package org.example.roommanager.service.impl;

import org.example.roommanager.dto.ApplicationRequest;
import org.example.roommanager.dto.ApplicationResponse;
import org.example.roommanager.entity.*;
import org.example.roommanager.exception.BusinessException;
import org.example.roommanager.repository.ClassroomRepository;
import org.example.roommanager.repository.RoomApplicationRepository;
import org.example.roommanager.repository.ScheduleRepository;
import org.example.roommanager.repository.UserRepository;
import org.example.roommanager.service.ApplicationService;
import org.example.roommanager.service.CourseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final RoomApplicationRepository appRepository;
    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;
    private final ScheduleRepository scheduleRepository;
    private  final CourseService courseService;

    public ApplicationServiceImpl(RoomApplicationRepository appRepository,
                                  UserRepository userRepository,
                                  ClassroomRepository classroomRepository, ScheduleRepository scheduleRepository, CourseService courseService) {
        this.appRepository = appRepository;
        this.userRepository = userRepository;
        this.classroomRepository = classroomRepository;
        this.scheduleRepository = scheduleRepository;
        this.courseService = courseService;
    }

    @Override
    @Transactional
    public ApplicationResponse createApplication(Long userId, ApplicationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));

        Classroom classroom = classroomRepository.findById(request.getClassroomId())
                .orElseThrow(() -> new BusinessException("CLASSROOM_NOT_FOUND", "教室不存在"));

        Course tempCourse = courseService.getCourseById(request.getCourseId());


        RoomApplication app = RoomApplication.builder()
                .userId(user.getId())
                .classroomId(classroom.getId())
                .date(request.getDate())
                .startSection(request.getStartSection())
                .endSection(request.getEndSection())
                .reason(request.getReason())
                .status("PENDING")
                .courseId(request.getCourseId())
                .courseName(tempCourse.getCourseName())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .applicantName(request.getApplicantName())
                .build();
        appRepository.save(app);

        return toResponse(app, user, classroom);
    }

    @Override
    public List<ApplicationResponse> listMyApplications(Long userId) {
        List<RoomApplication> list = appRepository.findByUserIdOrderByCreateTimeDesc(userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        return list.stream()
                .map(app -> {
                    Classroom c = classroomRepository.findById(app.getClassroomId())
                            .orElse(null);
                    return toResponse(app, user, c);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplicationResponse> listPendingApplications() {
        List<RoomApplication> list = appRepository.findByStatusOrderByCreateTimeAsc("PENDING");
        return list.stream()
                .map(app -> {
                    User user = userRepository.findById(app.getUserId()).orElse(null);
                    Classroom c = classroomRepository.findById(app.getClassroomId()).orElse(null);
                    return toResponse(app, user, c);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void approve(Long appId, Long adminId) {
        RoomApplication app = appRepository.findById(appId)
                .orElseThrow(() -> new BusinessException("APP_NOT_FOUND", "申请不存在"));

        if (!"PENDING".equals(app.getStatus())) {
            throw new BusinessException("APP_STATUS_ERROR", "申请已处理");
        }

        // 根据 date 计算 weekDay：1(周一) ~ 7(周日)
        LocalDate date = app.getDate();
        Schedule schedule = getSchedule(date, app);

        scheduleRepository.save(schedule);
        app.setStatus("APPROVED");
        app.setUpdateTime(LocalDateTime.now());
        appRepository.save(app);
    }

    private Schedule getSchedule(LocalDate date, RoomApplication app) {
        int weekDay = date.getDayOfWeek().getValue(); // MONDAY=1 ... SUNDAY=7
        // TODO: 这里可以检查是否有排课冲突，必要时生成 schedule 记录
        // 4. 创建排课记录 Schedule
        Schedule schedule = new Schedule();
        schedule.setClassroomId(app.getClassroomId());
        schedule.setDate(app.getDate());
        // 如果 Application 里有 weekDay 就用，没有可以算：
        schedule.setWeekDay(weekDay);
        schedule.setStartSection(app.getStartSection());
        schedule.setEndSection(app.getEndSection());

        // 关键：把申请里的课程名、申请人、理由写进 schedule
        schedule.setCourseName(app.getCourseName());
        schedule.setTeacherName(courseService.getCourseById(app.getCourseId()).getTeacherName());
        schedule.setReason(app.getReason());
        schedule.setCourseId(app.getCourseId());
        return schedule;
    }

    @Override
    @Transactional
    public void reject(Long appId, Long adminId) {
        RoomApplication app = appRepository.findById(appId)
                .orElseThrow(() -> new BusinessException("APP_NOT_FOUND", "申请不存在"));

        if (!"PENDING".equals(app.getStatus())) {
            throw new BusinessException("APP_STATUS_ERROR", "申请已处理");
        }
        app.setStatus("REJECTED");
        app.setUpdateTime(LocalDateTime.now());
        appRepository.save(app);
    }

    private ApplicationResponse toResponse(RoomApplication app, User user, Classroom classroom) {
        String username = user != null ? user.getUsername() : null;
        String classroomName = null;
        if (classroom != null) {
            classroomName = (classroom.getBuilding() != null ? classroom.getBuilding() : "")
                    + "-"
                    + (classroom.getRoomNumber() != null ? classroom.getRoomNumber() : "");
        }
        return new ApplicationResponse(
                app.getId(),
                app.getUserId(),
                username,
                app.getClassroomId(),
                classroomName,
                app.getDate(),
                app.getStartSection(),
                app.getEndSection(),
                app.getReason(),
                app.getStatus(),
                app.getCreateTime()
        );
    }
}