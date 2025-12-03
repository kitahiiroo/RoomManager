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
 * 使用 MockMvc 对 ClassroomController 做简单接口测试
 */
@SpringBootTest
@AutoConfigureMockMvc
class ClassroomControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;


    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // 每次测试前清空表，避免数据影响
        courseRepository.deleteAll();
        scheduleRepository.deleteAll();
        classroomRepository.deleteAll();
    }

    @Test
    void testCreateAndGetClassroom() throws Exception {
        Classroom classroom = Classroom.builder()
                .building("A栋")
                .roomNumber("301")
                .capacity(60)
                .hasProjector(true)
                .hasComputer(true)
                .status("AVAILABLE")
                .build();

        String json = objectMapper.writeValueAsString(classroom);

        // 调用创建接口
        String response = mockMvc.perform(post("/api/classrooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("X-Role", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.building", is("A栋")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Classroom saved = objectMapper.readValue(response, Classroom.class);

        // 再调用查询接口
        mockMvc.perform(get("/api/classrooms/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomNumber", is("301")))
                .andExpect(jsonPath("$.capacity", is(60)));
    }

    @Test
    void testListAvailableClassrooms() throws Exception {
        // 插入两条数据：一条可用，一条维护中
        Classroom room1 = Classroom.builder()
                .building("A栋")
                .roomNumber("101")
                .capacity(60)
                .hasProjector(true)
                .hasComputer(true)
                .status("AVAILABLE")
                .build();
        Classroom room2 = Classroom.builder()
                .building("A栋")
                .roomNumber("102")
                .capacity(40)
                .hasProjector(false)
                .hasComputer(false)
                .status("MAINTENANCE")
                .build();
        classroomRepository.save(room1);
        classroomRepository.save(room2);

        // 只查可用教室
        mockMvc.perform(get("/api/classrooms")
                        .param("onlyAvailable", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].roomNumber", is("101")));
    }

    @Test
    void testFindFreeClassroomsByDateAndSection() throws Exception {
        // 1. 插入一个课程
        Course course = courseRepository.save(
                Course.builder()
                        .courseName("测试课程")
                        .teacherName("测试老师")
                        .className("测试班级")
                        .build()
        );

        // 2. 插入两个教室
        Classroom room1 = classroomRepository.save(
                Classroom.builder()
                        .building("A栋")
                        .roomNumber("101")
                        .capacity(60)
                        .hasProjector(true)
                        .hasComputer(true)
                        .status("AVAILABLE")
                        .build()
        );
        Classroom room2 = classroomRepository.save(
                Classroom.builder()
                        .building("A栋")
                        .roomNumber("102")
                        .capacity(80)
                        .hasProjector(true)
                        .hasComputer(true)
                        .status("AVAILABLE")
                        .build()
        );

        // 3. 插入一条排课：room1 在 2025-06-01 第1-2节有课
        Schedule schedule = Schedule.builder()
                .classroomId(room1.getId())
                .courseId(course.getId())   // 用真实存在的课程ID
                .date(LocalDate.of(2025, 6, 1))
                .weekDay(1)
                .startSection(1)
                .endSection(2)
                .build();
        scheduleRepository.save(schedule);

        // 4. 查询 2025-06-01 第1-2节、容量>=50 的空闲教室
        mockMvc.perform(get("/api/classrooms/free")
                        .param("date", "2025-06-01")
                        .param("startSection", "1")
                        .param("endSection", "2")
                        .param("minCapacity", "50"))
                .andExpect(status().isOk())
                // 应该只有 room2 空闲
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].roomNumber", is("102")));
    }
}