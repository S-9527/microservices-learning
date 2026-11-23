package com.example.microservices.user.controller;

import com.example.microservices.common.ApiResponse;
import com.example.microservices.common.dto.UserInfo;
import com.example.microservices.user.entity.User;
import com.example.microservices.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** order-service 通过 Feign 调用这个接口。 */
    @GetMapping("/users/{id}")
    public ApiResponse<UserInfo> getUser(@PathVariable("id") Long id) {
        return ApiResponse.ok(toInfo(userService.getById(id)));
    }

    @GetMapping("/users/username/{username}")
    public ApiResponse<UserInfo> getByUsername(@PathVariable("username") String username) {
        return ApiResponse.ok(toInfo(userService.getByUsername(username)));
    }

    @PostMapping("/users")
    public ApiResponse<UserInfo> createUser(
            @RequestParam("username") String username,
            @RequestParam("nickname") String nickname,
            @RequestParam("email") String email) {
        return ApiResponse.ok(toInfo(userService.create(username, nickname, email)));
    }

    private UserInfo toInfo(User user) {
        return new UserInfo(user.getId(), user.getNickname(), user.getEmail());
    }
}
