package org.example.roommanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.roommanager.entity.Course;
import org.example.roommanager.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 使用 MockMvc 对 CourseController 做接口测试
 */
@SpringBootTest
@AutoConfigureMockMvc
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        courseRepository.deleteAll();
    }

    @Test
    void testCreateAndGetCourse() throws Exception {
        Course course = Course.builder()
                .courseName("高等数学")
                .teacherName("张三")
                .className("计科2201")
                .build();

        String json = objectMapper.writeValueAsString(course);

        // 创建课程
        String response = mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.courseName", is("高等数学")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Course saved = objectMapper.readValue(response, Course.class);

        // 按ID查询课程
        mockMvc.perform(get("/api/courses/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teacherName", is("张三")))
                .andExpect(jsonPath("$.className", is("计科2201")));
    }

    @Test
    void testListCourseByTeacher() throws Exception {
        Course c1 = Course.builder()
                .courseName("高等数学")
                .teacherName("张三")
                .className("计科2201")
                .build();
        Course c2 = Course.builder()
                .courseName("线性代数")
                .teacherName("李四")
                .className("计科2202")
                .build();
        courseRepository.save(c1);
        courseRepository.save(c2);

        // 按老师查询课程
        mockMvc.perform(get("/api/courses")
                        .param("teacherName", "张三"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].courseName", is("高等数学")));
    }

    @Test
    void testListCourseByClassName() throws Exception {
        Course c1 = Course.builder()
                .courseName("操作系统")
                .teacherName("王五")
                .className("计科2201")
                .build();
        Course c2 = Course.builder()
                .courseName("计算机网络")
                .teacherName("王五")
                .className("计科2202")
                .build();
        courseRepository.save(c1);
        courseRepository.save(c2);

        // 按班级查询课程
        mockMvc.perform(get("/api/courses")
                        .param("className", "计科2201"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].courseName", is("操作系统")));
    }
}