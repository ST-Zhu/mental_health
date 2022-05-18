package com.example.mental_health;

import com.example.mental_health.pojo.User;
import com.example.mental_health.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;

@SpringBootTest
@Slf4j
class MentalHealthApplicationTests {
    @Autowired
    UserService userService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void contextLoads() {
        User user = new User();
        user.setUsername("admin");
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRole("admin");
        user.setEmail("admin@123.com");
        user.setNickname("管理员");
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        log.info("保存：{}", userService.save(user));
    }

    @Test
    void contextLoads2() {
        User user = new User();
        user.setUsername("doctor2");
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRole("doctor");
        user.setEmail("doctor2@123.com");
        user.setNickname("医生1");
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        log.info("保存：{}", userService.save(user));
    }

}
