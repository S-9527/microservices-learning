package com.example.microservices.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 与 user_db.users 表一一对应的实体（getter/setter/toString 由 Lombok 生成）。 */
@Data
@TableName("users")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private LocalDateTime createdAt;
}
