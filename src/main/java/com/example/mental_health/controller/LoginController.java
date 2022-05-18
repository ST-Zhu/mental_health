package com.example.mental_health.controller;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.mental_health.exception.CustomException;
import com.example.mental_health.pojo.DetailUser;
import com.example.mental_health.pojo.User;
import com.example.mental_health.result.Result;
import com.example.mental_health.service.DetailUserService;
import com.example.mental_health.service.UserService;
import com.example.mental_health.util.ReplaceBackground;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;


@Controller
public class LoginController {

    @Autowired
    UserService userService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    DetailUserService detailUserService;

    @GetMapping(value = {"/","/login_page"})
    public String loginPage(Model model) {
        model.addAttribute("imgBase64Str", ReplaceBackground.replaceImg());
        return "login_page";
    }

    @ResponseBody
    @PostMapping("/register")
    public String register(@Validated User user, String passwordAgain) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", user.getUsername());
        long count = userService.count(wrapper);
        if (count > 0) {
            throw new CustomException("该用户名已存在，请重新输入");
        }
        if (!user.getPassword().equals(passwordAgain)) {
            throw new CustomException("两次密码不相等，请重新输入");
        }
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.save(user);
        DetailUser detailUser = new DetailUser();
        detailUser.setUsername(user.getUsername());
        detailUser.setRole(user.getRole());
        detailUserService.save(detailUser);
        return JSON.toJSONString(Result.buildSuccess("注册成功"));
    }

    @RequestMapping("/login")
    public String login(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getOne(new QueryWrapper<User>().eq("username", userDetails.getUsername()));
        String role = user.getRole();
        if ("admin".equals(role)) {
            return "redirect:/admin/index";
        }
        if ("user".equals(role)) {
            return "redirect:/user/index";
        }
        if ("doctor".equals(role)) {
            return "redirect:/doctor/index";
        }
        return "login_page";
    }
}
