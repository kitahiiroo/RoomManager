-- 先删子表，再删父表

DROP TABLE IF EXISTS schedule;
DROP TABLE IF EXISTS course;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS classroom;

CREATE TABLE classroom
(
    id            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    building      VARCHAR(50) NOT NULL COMMENT '楼栋名称，如A栋',
    room_number   VARCHAR(20) NOT NULL COMMENT '教室号，如101',
    capacity      INT         NOT NULL COMMENT '可容纳人数',
    has_projector TINYINT(1)  NOT NULL COMMENT '是否有投影仪：0-否 1-是',
    has_computer  TINYINT(1)  NOT NULL COMMENT '是否有多媒体/电脑：0-否 1-是',
    status        VARCHAR(20) NOT NULL COMMENT '状态：AVAILABLE/MAINTENANCE等',
    PRIMARY KEY (id),
    INDEX idx_classroom_building (building),
    INDEX idx_classroom_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='教室信息表';

CREATE TABLE course
(
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    course_name  VARCHAR(100) NOT NULL COMMENT '课程名称',
    teacher_name VARCHAR(50)  NOT NULL COMMENT '任课教师姓名',
    class_name   VARCHAR(50)  NOT NULL COMMENT '上课班级',
    PRIMARY KEY (id),
    INDEX idx_course_teacher_name (teacher_name),
    INDEX idx_course_class_name (class_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='课程信息表';

CREATE TABLE schedule
(
    id            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    classroom_id  BIGINT NOT NULL COMMENT '教室ID，关联classroom.id',
    course_id     BIGINT NOT NULL COMMENT '课程ID，关联course.id',
    date          DATE   NOT NULL COMMENT '上课日期',
    week_day      INT    NOT NULL COMMENT '周几：1-7',
    start_section INT    NOT NULL COMMENT '开始节次，例如1',
    end_section   INT    NOT NULL COMMENT '结束节次，例如2',
    PRIMARY KEY (id),
    INDEX idx_schedule_classroom_date (classroom_id, date),
    INDEX idx_schedule_date_section (date, start_section, end_section),
    CONSTRAINT fk_schedule_classroom
        FOREIGN KEY (classroom_id) REFERENCES classroom (id)
            ON DELETE CASCADE
            ON UPDATE CASCADE,
    CONSTRAINT fk_schedule_course
        FOREIGN KEY (course_id) REFERENCES course (id)
            ON DELETE CASCADE
            ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='排课/教室占用表';

CREATE TABLE sys_user
(
    id       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50)  NOT NULL COMMENT '登录用户名',
    password VARCHAR(100) NOT NULL COMMENT '登录密码',
    role     VARCHAR(20)  NOT NULL COMMENT '角色：ADMIN/TEACHER/STUDENT',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='系统用户表';