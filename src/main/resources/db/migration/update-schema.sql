CREATE TABLE classroom
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    building      VARCHAR(50) NOT NULL,
    room_number   VARCHAR(20) NOT NULL,
    capacity      INT         NOT NULL,
    has_projector BIT(1)      NOT NULL,
    has_computer  BIT(1)      NOT NULL,
    status        VARCHAR(20) NOT NULL,
    CONSTRAINT pk_classroom PRIMARY KEY (id)
);

CREATE TABLE course
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    course_name  VARCHAR(100) NOT NULL,
    teacher_name VARCHAR(50)  NOT NULL,
    class_name   VARCHAR(50)  NOT NULL,
    CONSTRAINT pk_course PRIMARY KEY (id)
);

CREATE TABLE room_application
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    user_id        BIGINT      NOT NULL,
    classroom_id   BIGINT      NOT NULL,
    date           date        NOT NULL,
    start_section  INT         NOT NULL,
    end_section    INT         NOT NULL,
    reason         VARCHAR(255) NULL,
    status         VARCHAR(20) NOT NULL,
    create_time    datetime    NOT NULL,
    update_time    datetime    NOT NULL,
    applicant_name VARCHAR(255) NULL,
    course_name    VARCHAR(255) NULL,
    CONSTRAINT pk_room_application PRIMARY KEY (id)
);

CREATE TABLE schedule
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    classroom_id  BIGINT NOT NULL,
    course_id     BIGINT NOT NULL,
    date          date   NOT NULL,
    week_day      INT    NOT NULL,
    start_section INT    NOT NULL,
    end_section   INT    NOT NULL,
    reason        VARCHAR(255) NULL,
    course_name   VARCHAR(255) NULL,
    teacher_name  VARCHAR(255) NULL,
    CONSTRAINT pk_schedule PRIMARY KEY (id)
);

CREATE TABLE user
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    username    VARCHAR(50)  NOT NULL,
    password    VARCHAR(100) NOT NULL,
    `role`      VARCHAR(20)  NOT NULL,
    create_time datetime     NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id)
);

ALTER TABLE user
    ADD CONSTRAINT uc_user_username UNIQUE (username);