package org.example.roommanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.roommanager.entity.Classroom;
import org.example.roommanager.entity.Course;
import org.example.roommanager.entity.Schedule;
import org.example.roommanager.repository.ClassroomRepository;
import org.example.roommanager.repository.CourseRepository;
import org.example.roommanager.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 使用 MockMvc 对 ScheduleController 做接口测试
 */
@SpringBootTest
@AutoConfigureMockMvc
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    ClassroomRepository classroomRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        scheduleRepository.deleteAll();
    }


    @Test
    void testCreateScheduleConflict() throws Exception {
// 先建课程和教室
        Course course1 = courseRepository.save(
                Course.builder()
                        .courseName("测试课程1")
                        .teacherName("测试老师1")
                        .className("测试班级1")
                        .build()
        );

        Course course2 = courseRepository.save(
                Course.builder()
                        .courseName("测试课程2")
                        .teacherName("测试老师2")
                        .className("测试班级2")
                        .build()
        );

        Classroom classroom = classroomRepository.save(
                Classroom.builder()
                        .building("A栋")
                        .roomNumber("101")
                        .capacity(60)
                        .hasProjector(true)
                        .hasComputer(true)
                        .status("AVAILABLE")
                        .build()
        );

        Classroom classroom2 = classroomRepository.save(
                Classroom.builder()
                        .building("B栋")
                        .roomNumber("301")
                        .capacity(60)
                        .hasProjector(true)
                        .hasComputer(true)
                        .status("AVAILABLE")
                        .build()
        );

        // 先插入一个已有排课
        Schedule s1 = scheduleRepository.save(
                Schedule.builder()
                        .classroomId(classroom.getId())
                        .courseId(course1.getId())
                        .date(LocalDate.of(2025, 6, 1))
                        .weekDay(1)
                        .startSection(1)
                        .endSection(2)
                        .build()
        );

        // 再构造一个有冲突的排课请求
        Schedule s2 = Schedule.builder()
                .classroomId(classroom.getId())
                .courseId(course2.getId())
                .date(LocalDate.of(2025, 6, 1))
                .weekDay(1)
                .startSection(2) // 与 1-2 节有重叠
                .endSection(3)
                .build();

        String json = objectMapper.writeValueAsString(s2);

        mockMvc.perform(post("/api/schedules")
                        .header("X-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("SCHEDULE_CONFLICT")));
    }
    @Test
    void testCreateAndGetSchedule() throws Exception {
        Schedule schedule = Schedule.builder()
                .classroomId(1L)
                .courseId(1L)
                .date(LocalDate.of(2025, 6, 1))
                .weekDay(1)
                .startSection(1)
                .endSection(2)
                .build();

        String json = objectMapper.writeValueAsString(schedule);

        // 创建排课记录
        String response = mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.classroomId", is(1)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Schedule saved = objectMapper.readValue(response, Schedule.class);

        // 按ID查询排课记录
        mockMvc.perform(get("/api/schedules/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startSection", is(1)))
                .andExpect(jsonPath("$.endSection", is(2)));
    }

    @Test
    void testListSchedulesByClassroomAndDate() throws Exception {
        // 插入两条同一天的排课，不同教室
        Schedule s1 = Schedule.builder()
                .classroomId(1L)
                .courseId(1L)
                .date(LocalDate.of(2025, 6, 1))
                .weekDay(1)
                .startSection(1)
                .endSection(2)
                .build();
        Schedule s2 = Schedule.builder()
                .classroomId(2L)
                .courseId(2L)
                .date(LocalDate.of(2025, 6, 1))
                .weekDay(1)
                .startSection(3)
                .endSection(4)
                .build();
        scheduleRepository.save(s1);
        scheduleRepository.save(s2);

        // 查询教室1在 2025-06-01 的排课
        mockMvc.perform(get("/api/schedules")
                        .param("classroomId", "1")
                        .param("date", "2025-06-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].classroomId", is(1)))
                .andExpect(jsonPath("$[0].startSection", is(1)));
    }

    @Test
    void testListSchedulesByDateAndSectionRange() throws Exception {
        // 插入三条记录：两条在1-2节有交集，一条在5-6节
        Schedule a = Schedule.builder()
                .classroomId(1L)
                .courseId(1L)
                .date(LocalDate.of(2025, 6, 1))
                .weekDay(1)
                .startSection(1)
                .endSection(2)
                .build();
        Schedule b = Schedule.builder()
                .classroomId(2L)
                .courseId(2L)
                .date(LocalDate.of(2025, 6, 1))
                .weekDay(1)
                .startSection(2)
                .endSection(3)
                .build();
        Schedule c = Schedule.builder()
                .classroomId(3L)
                .courseId(3L)
                .date(LocalDate.of(2025, 6, 1))
                .weekDay(1)
                .startSection(5)
                .endSection(6)
                .build();
        scheduleRepository.save(a);
        scheduleRepository.save(b);
        scheduleRepository.save(c);

        // 查询 2025-06-01 第1-2节有交集的排课
        mockMvc.perform(get("/api/schedules")
                        .param("date", "2025-06-01")
                        .param("startSection", "1")
                        .param("endSection", "2"))
                .andExpect(status().isOk())
                // a: 1-2, b: 2-3 都和 1-2 有交集，c: 5-6 没有
                .andExpect(jsonPath("$", hasSize(2)));
    }
}