package org.example.roommanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统用户（管理员/教师/学生）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sys_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 登录用户名
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 登录密码（课设可以不加密或简单加密）
     */
    @Column(nullable = false, length = 100)
    private String password;

    /**
     * 角色：ADMIN / TEACHER / STUDENT
     */
    @Column(nullable = false, length = 20)
    private String role;
}
