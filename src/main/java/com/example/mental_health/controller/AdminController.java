package com.example.mental_health.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.mental_health.exception.CustomException;
import com.example.mental_health.pojo.*;
import com.example.mental_health.pojo.vo.GradeLevel;
import com.example.mental_health.result.Result;
import com.example.mental_health.service.*;
import com.sun.org.apache.xpath.internal.operations.Mod;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    UserService userService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    DetailUserService detailUserService;

    @Autowired
    SendMsgService sendMsgService;

    @Autowired
    AppointService appointService;

    @Autowired
    ScaleService scaleService;

    @Autowired
    QuestionService questionService;

    @Autowired
    UserScaleService userScaleService;


    @GetMapping("/index")
    public String index(Model model) {
        Map<String, GradeLevel> map = new HashMap<>();
        List<Scale> list = scaleService.list();
        for (Scale scale : list) {
            int full = scale.getScoreFull();
            long count1 = userScaleService.count(new QueryWrapper<UserScale>().eq("scale_name", scale.getName())
                    .between("grade", 0, full/3));
            long count2 = userScaleService.count(new QueryWrapper<UserScale>().eq("scale_name", scale.getName())
                    .between("grade", full/3, (full/3)*2));
            long count3 = userScaleService.count(new QueryWrapper<UserScale>().eq("scale_name", scale.getName())
                    .between("grade", (full/3)*2, full));

            map.put(scale.getName(), new GradeLevel(count1, count2, count3));
        }
        model.addAttribute("map", map);
        return "index_admin";
    }

    @GetMapping("/queryAccount")
    public String queryAccount(Model model, String searchName, String role) {
        List<User> list;
        if ((searchName == null) || (searchName.isEmpty())) {
            list = userService.list(new QueryWrapper<User>().eq("role", role));
        } else {
            list = userService.list(new QueryWrapper<User>().eq("role", role).like("username", searchName));
        }
        if (list.size() == 0) {
            model.addAttribute("message", "暂无账号信息！");
        } else {
            model.addAttribute("message", "账号信息：");
            model.addAttribute("UserList", list);
        }
        model.addAttribute("searchName", searchName);
        model.addAttribute("role", role);
        return "manage_account";
    }

    @PostMapping("/deleteAccount")
    @ResponseBody
    public String deleteAccount(String id) {
        //TODO 删除其他表中的数据
        User user = userService.getById(id);
        detailUserService.remove(new QueryWrapper<DetailUser>().eq("username", user.getUsername()));
        appointService.remove(new QueryWrapper<Appoint>().eq("doctor_username", user.getUsername()));
        sendMsgService.remove(new QueryWrapper<SendMsg>().eq("sender_username", user.getUsername()));
        sendMsgService.remove(new QueryWrapper<SendMsg>().eq("receiver_username", user.getUsername()));
        userScaleService.remove(new QueryWrapper<UserScale>().eq("user_username", user.getUsername()));

        boolean b = userService.removeById(id);
        if (!b) {
            throw new CustomException("删除失败");
        }
        return JSON.toJSONString(Result.buildSuccess("删除成功"));
    }

    @PostMapping("/insertAccount")
    @ResponseBody
    public String insertAccount(@Validated User user, String passwordAgain) {
        if (!user.getPassword().equals(passwordAgain)) {
            throw new CustomException("两次密码不相等，请重新输入");
        }
        if (user.getId() == null) {
            long count = userService.count(new QueryWrapper<User>().eq("username", user.getUsername()));
            if (count > 0) {
                throw new CustomException("该用户名已存在，请重新输入");
            }
        }
        user.setUpdateTime(new Date());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getId() == null) {
            user.setCreateTime(new Date());
            userService.save(user);
            DetailUser detailUser = new DetailUser();
            detailUser.setUsername(user.getUsername());
            detailUser.setRole(user.getRole());
            detailUserService.save(detailUser);
            return JSON.toJSONString(Result.buildSuccess("添加成功"));
        } else {
            String username = userService.getById(user.getId()).getUsername();
            userService.updateById(user); // 创建时间为空，不会修改
            if (!username.equals(user.getUsername())) {
                //TODO 修改其他表中的数据
                detailUserService.update(new UpdateWrapper<DetailUser>().set("username", user.getUsername()).eq("username", username));
                appointService.update(new UpdateWrapper<Appoint>().set("doctor_username", user.getUsername()).eq("doctor_username", username));
                sendMsgService.update(new UpdateWrapper<SendMsg>().set("sender_username", user.getUsername()).eq("sender_username", username));
                sendMsgService.update(new UpdateWrapper<SendMsg>().set("receiver_username", user.getUsername()).eq("receiver_username", username));
                userScaleService.update(new UpdateWrapper<UserScale>().set("user_username", user.getUsername()).eq("user_username", username));
            }
            return JSON.toJSONString(Result.buildSuccess("修改成功"));
        }
    }

    @GetMapping("/account")
    public String account(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.getOne(new QueryWrapper<User>().eq("username", userDetails.getUsername()));
        model.addAttribute("user", user);
        return "account_admin";
    }

    @ResponseBody
    @PostMapping("/updateAccount")
    public String updateAccount(User user, String passwordAgain) {
        if ((user.getPassword() == null) || (user.getPassword().isEmpty())) {
            throw new CustomException("密码不能为空");
        }
        if (!user.getPassword().equals(passwordAgain)) {
            throw new CustomException("两次密码不相等，请重新输入");
        }
        user.setUpdateTime(new Date());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        boolean b = userService.updateById(user);
        if (!b) {
            throw new CustomException("修改失败");
        }
        return JSON.toJSONString(Result.buildSuccess("修改成功"));
    }

    @GetMapping("queryScale")
    public String queryScale(Model model, String searchName) {
        List<Scale> scaleList;
        if ((searchName == null) || (searchName.isEmpty())) {
            scaleList = scaleService.list();
        } else {
            scaleList = scaleService.list(new QueryWrapper<Scale>().like("name", searchName));
        }
        if (scaleList.size() == 0) {
            model.addAttribute("message", "暂无信息！");
        } else {
            model.addAttribute("message", "信息如下：");
            model.addAttribute("scaleList", scaleList);
        }
        model.addAttribute("searchName", searchName);
        return "manage_scale";
    }

    @ResponseBody
    @PostMapping("deleteScale")
    public String deleteScale(String id) {
        //TODO 删除其他表中的数据
        Scale scale = scaleService.getById(id);
        userScaleService.remove(new QueryWrapper<UserScale>().eq("scale_name", scale.getName()));
        questionService.remove(new QueryWrapper<Question>().eq("scale_name", scale.getName()));

        boolean b = scaleService.removeById(id);
        if (!b) {
            throw new CustomException("删除失败");
        }
        return JSON.toJSONString(Result.buildSuccess("删除成功"));
    }

    @ResponseBody
    @PostMapping("insertScale")
    public String insertScale(Scale scale) {
        if ((scale.getName() == null) || (scale.getName().isEmpty())) {
            throw new CustomException("量表名不能为空");
        }
        if (scale.getId() == null) {
            long count = scaleService.count(new QueryWrapper<Scale>().eq("name", scale.getName()));
            if (count > 0) {
                throw new CustomException("量表的名字已存在，请重新命名");
            }
            scale.setCreateTime(new Date());
        } else {
            //TODO 修改其他表中的数据
            String scaleName = scaleService.getById(scale.getId()).getName();
            if (!scale.getName().equals(scaleName)) {
                userScaleService.update(new UpdateWrapper<UserScale>().set("scale_name", scale.getName()).eq("scale_name", scaleName));
                questionService.update(new UpdateWrapper<Question>().set("scale_name", scale.getName()).eq("scale_name", scaleName));
            }
        }
        scale.setUpdateTime(new Date());
        boolean b = scaleService.saveOrUpdate(scale);
        if (!b) {
            throw new CustomException("失败");
        }
        return JSON.toJSONString(Result.buildSuccess("成功"));
    }

    @GetMapping("queryQuestion")
    public String queryQuestion(String searchName, Model model, String scaleName) {
        List<Question> list;
        if ((searchName == null) || (searchName.isEmpty())) {
            list = questionService.list(new QueryWrapper<Question>().eq("scale_name", scaleName));
        } else {
            list = questionService.list(new QueryWrapper<Question>()
                    .eq("scale_name", scaleName)
                    .like("theme", searchName));
        }
        if (list.size() == 0) {
            model.addAttribute("message", "暂无信息！");
        } else {
            model.addAttribute("message", "信息如下：");
            model.addAttribute("list", list);
        }
        model.addAttribute("searchName", searchName);
        model.addAttribute("scaleName", scaleName);
        return "manage_question";
    }

    @ResponseBody
    @PostMapping("deleteQuestion")
    public String deleteQuestion(String id) {
        boolean b = questionService.removeById(id);
        if (!b) {
            throw new CustomException("删除失败");
        }
        return JSON.toJSONString(Result.buildSuccess("删除成功"));
    }

    @ResponseBody
    @PostMapping("insertQuestion")
    public String insertQuestion(Question question) {
        if (question.getId() == null) {
            question.setCreateTime(new Date());
        }
        question.setUpdateTime(new Date());
        boolean b = questionService.saveOrUpdate(question);
        if (!b) {
            throw new CustomException("失败");
        }
        return JSON.toJSONString(Result.buildSuccess("成功"));
    }
}
