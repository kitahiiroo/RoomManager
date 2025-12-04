package org.example.roommanager.controller;

import jakarta.servlet.http.HttpSession;
import org.example.roommanager.auth.LoginUserContext;
import org.example.roommanager.dto.*;
import org.example.roommanager.entity.Classroom;
import org.example.roommanager.exception.BusinessException;
import org.example.roommanager.service.ApplicationService;
import org.example.roommanager.service.ClassroomService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;


    private final ClassroomService classroomService;

    public ApplicationController(ApplicationService applicationService,
                                 ClassroomService classroomService) {
        this.applicationService = applicationService;
        this.classroomService = classroomService;
    }

    /**
     * 新增申请
     */
    @PostMapping
    public ApplicationResponse create(@RequestBody ApplicationRequest req) {
        var user = LoginUserContext.requireLogin();

        ApplicationRequest app = new ApplicationRequest();
        app.setUserId(user.getId());
        app.setClassroomId(req.getClassroomId());
        app.setDate(req.getDate());
        app.setStartSection(req.getStartSection());
        app.setEndSection(req.getEndSection());
        app.setReason(req.getReason());
        app.setCourseId(req.getCourseId());
        app.setCourseName(req.getCourseName());
        app.setApplicantName(req.getApplicantName());

        // 关键：把申请人、课程名写到申请对象
        app.setApplicantName(req.getApplicantName());
        app.setCourseName(req.getCourseName());

        ApplicationResponse saved = applicationService.createApplication(app.getUserId(),app);
        Classroom classroom = classroomService.getClassroomById(saved.getClassroomId());

        ApplicationResponse resp = new ApplicationResponse();
        resp.setId(saved.getId());
        resp.setUserId(saved.getUserId());
        resp.setUsername(user.getUsername());
        resp.setClassroomId(saved.getClassroomId());
        resp.setClassroomName(classroom.getBuilding() + "-" + classroom.getRoomNumber());
        resp.setDate(saved.getDate());
        resp.setStartSection(saved.getStartSection());
        resp.setEndSection(saved.getEndSection());
        resp.setReason(saved.getReason());
        resp.setStatus(saved.getStatus());
        resp.setCreateTime(saved.getCreateTime());
        return resp;
    }

    /**
     * 查询当前用户的申请列表
     */
    @GetMapping("/mine")
    public List<ApplicationResponse> listMine() {
        var user = LoginUserContext.requireLogin();
        List<ApplicationResponse> list = applicationService.listMyApplications(user.getId());

        return list.stream()
                .map(app -> {
                    Classroom classroom = classroomService.getClassroomById(app.getClassroomId());
                    ApplicationResponse resp = new ApplicationResponse();
                    resp.setId(app.getId());
                    resp.setUserId(app.getUserId());
                    resp.setUsername(user.getUsername());
                    resp.setClassroomId(app.getClassroomId());
                    resp.setClassroomName(classroom.getBuilding() + "-" + classroom.getRoomNumber());
                    resp.setDate(app.getDate());
                    resp.setStartSection(app.getStartSection());
                    resp.setEndSection(app.getEndSection());
                    resp.setReason(app.getReason());
                    resp.setStatus(app.getStatus());
                    resp.setCreateTime(app.getCreateTime());
                    return resp;
                })
                .collect(Collectors.toList());
    }

    /**
     * 管理员：查询待审批列表
     */
    @GetMapping("/pending")
    public List<ApplicationResponse> listPending() {
        var admin = LoginUserContext.requireAdmin();
        List<ApplicationResponse> list = applicationService.listPendingApplications();

        return list.stream()
                .map(app -> {
                    Classroom classroom = classroomService.getClassroomById(app.getClassroomId());
                    ApplicationResponse resp = new ApplicationResponse();
                    resp.setId(app.getId());
                    resp.setUserId(app.getUserId());
                    // 这里如果你有 userService 可以查出真正的 username
                    resp.setUsername("用户" + app.getUserId());
                    resp.setClassroomId(app.getClassroomId());
                    resp.setClassroomName(classroom.getBuilding() + "-" + classroom.getRoomNumber());
                    resp.setDate(app.getDate());
                    resp.setStartSection(app.getStartSection());
                    resp.setEndSection(app.getEndSection());
                    resp.setReason(app.getReason());
                    resp.setStatus(app.getStatus());
                    resp.setCreateTime(app.getCreateTime());
                    return resp;
                })
                .collect(Collectors.toList());
    }

    // 普通用户：查看自己的申请
    @GetMapping("/my")
    public List<ApplicationResponse> myApplications() {
        var loginUser = LoginUserContext.requireLogin();
        return applicationService.listMyApplications(loginUser.getId());
    }

//    // 管理员：查看待审批申请
//    @GetMapping("/pending")
//    public List<ApplicationResponse> pending() {
//        LoginUserContext.requireAdmin(); // 只要通过就说明是 ADMIN
//        return applicationService.listPendingApplications();
//    }

    // 管理员：审批通过
    @PostMapping("/{id}/approve")
    public void approve(@PathVariable("id") Long id) {
        var admin = LoginUserContext.requireAdmin();
        applicationService.approve(id, admin.getId());
    }

    // 管理员：审批拒绝
    @PostMapping("/{id}/reject")
    public void reject(@PathVariable("id") Long id, HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (loginUser == null) {
            throw new BusinessException("UNAUTHORIZED", "未登录");
        }
        if (!"ADMIN".equals(loginUser.getRole())) {
            throw new BusinessException("FORBIDDEN", "需要管理员权限");
        }
        applicationService.reject(id, loginUser.getId());
    }
}