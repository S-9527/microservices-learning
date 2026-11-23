package com.example.microservices.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.microservices.common.BusinessException;
import com.example.microservices.common.ErrorCode;
import com.example.microservices.user.entity.User;
import com.example.microservices.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

/** 用户领域逻辑：真实读写 MySQL，不再使用内存 Map。 */
@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public User getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在: " + id);
        }
        return user;
    }

    public User getByUsername(String username) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在: " + username);
        }
        return user;
    }

    public User create(String username, String nickname, String email) {
        User user = new User();
        user.setUsername(username);
        user.setNickname(nickname);
        user.setEmail(email);
        userMapper.insert(user);
        return user;
    }
}
