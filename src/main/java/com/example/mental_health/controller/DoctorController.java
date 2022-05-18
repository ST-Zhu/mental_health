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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired
    AppointService appointService;

    @Autowired
    DetailUserService detailUserService;

    @Autowired
    SendMsgService sendMsgService;

    @Autowired
    UserService userService;

    @Autowired
    PasswordEncoder passwordEncoder;

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
        return "index_doctor";
    }


    @GetMapping("/healthTest")
    public String healthTest(Model model) {
        return queryScale(model, null);
    }

    @GetMapping("/historyRecord")
    public String historyRecord(String searchName, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        List<Appoint> appointList = appointService.list(new QueryWrapper<Appoint>()
                .eq("doctor_username", userDetails.getUsername()).eq("state", "完成"));
        Map<Date, DetailUser> detailUserMap = new HashMap<>();
        for (Appoint appoint : appointList) {
            DetailUser detailUser;
            if ((searchName == null) || (searchName.isEmpty())) {
                detailUser = detailUserService.getOne(new QueryWrapper<DetailUser>().eq("username", appoint.getUserUsername()));
            } else {
                detailUser = detailUserService.getOne(new QueryWrapper<DetailUser>().eq("username", appoint.getUserUsername())
                        .like("name", searchName));
            }
            if (detailUser != null) {
                detailUserMap.put(appoint.getCompleteTime(), detailUser);
            }
        }
        model.addAttribute("searchName", searchName);
        if (detailUserMap.size() == 0) {
            model.addAttribute("message", "暂无用户信息！");
        } else {
            model.addAttribute("message", "用户信息如下：");
            model.addAttribute("detailUserMap", detailUserMap);
        }
        return "history_doctor";
    }

    @GetMapping("/queryUser")
    public String queryUser(String searchName, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        List<Appoint> appointList = appointService.list(new QueryWrapper<Appoint>()
                .eq("doctor_username", userDetails.getUsername()).ne("state", "完成"));
        Map<Date,DetailUser> appointMap = new HashMap();
        Map<Date,DetailUser> consultMap = new HashMap();
        for (Appoint appoint : appointList) {
            DetailUser detailUser;
            if ((searchName == null) || (searchName.isEmpty())) {
                detailUser = detailUserService.getOne(new QueryWrapper<DetailUser>().eq("username", appoint.getUserUsername()));
            } else {
                detailUser = detailUserService.getOne(new QueryWrapper<DetailUser>()
                        .eq("username", appoint.getUserUsername())
                        .like("name", searchName));
            }
            if (detailUser != null) {
                if ("预约中".equals(appoint.getState())) {
                    appointMap.put(appoint.getCreateTime(), detailUser);
                } else if ("咨询中".equals(appoint.getState())) {
                    consultMap.put(appoint.getCreateTime(), detailUser);
                }

            }
        }

        model.addAttribute("searchName", searchName);
        if ((appointMap.size() + consultMap.size()) == 0) {
            model.addAttribute("message", "暂无用户信息！");
        } else {
            model.addAttribute("message", "用户信息如下：");
            model.addAttribute("appointMap", appointMap);
            model.addAttribute("consultMap", consultMap);

        }
        return "appointServe_doctor";
    }

    @PostMapping("/sendMsg")
    @ResponseBody
    public String sendMsg(@AuthenticationPrincipal UserDetails userDetails,
                          String username, String sendMsg) {
        if ((username == null) || (username.isEmpty())) {
            throw new CustomException("接收的用户名信息不能为空");
        }
        if ((sendMsg == null) || (sendMsg.isEmpty())) {
            throw new CustomException("发送信息真的不能为空");
        }
        boolean save = sendMsgService.save(new SendMsg(null, userDetails.getUsername(), username, sendMsg,
                null, new Date(), null, null, null));
        if (save) {
            return JSON.toJSONString(Result.buildSuccess("消息发送成功"));
        }
        throw new CustomException("消息发送失败");
    }

    @GetMapping("/personalCenter")
    public String personalCenter(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        DetailUser detailUser = detailUserService.getOne(new QueryWrapper<DetailUser>().eq("username", userDetails.getUsername()));
        long count = appointService.count(new QueryWrapper<Appoint>().eq("doctor_username", userDetails.getUsername())
                .eq("state", "预约中"));
        long count2 = appointService.count(new QueryWrapper<Appoint>().eq("doctor_username", userDetails.getUsername())
                .eq("state", "完成"));
        detailUser.setAppointNum(count);
        detailUser.setCompleteNum(count2);
        detailUserService.updateById(detailUser);
        model.addAttribute("detailUser", detailUser);
        return "detail_doctor";
    }

    @ResponseBody
    @PostMapping("/updateDetail")
    public String updateDetail(DetailUser detailUser) {
        boolean b = detailUserService.updateById(detailUser);
        if (!b) {
            throw new CustomException("修改失败");
        }
        return JSON.toJSONString(Result.buildSuccess("修改成功"));
    }

    @GetMapping("/account")
    public String account(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.getOne(new QueryWrapper<User>().eq("username", userDetails.getUsername()));
        model.addAttribute("user", user);
        return "account_doctor";
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
        return "scale_doctor";
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
        return "question_doctor";
    }

    @GetMapping("dealAppoint")
    public String dealAppoint(Model model, String username, @AuthenticationPrincipal UserDetails userDetails, String searchName) {
        List<SendMsg> list;
        appointService.update(new UpdateWrapper<Appoint>().set("state","咨询中")
                .eq("user_username", username)
                .eq("doctor_username", userDetails.getUsername())
                .eq("state", "预约中"));
        if ((searchName == null) || (searchName.isEmpty())) {
            list = sendMsgService.list(new QueryWrapper<SendMsg>().eq("sender_username", username)
                    .eq("receiver_username", userDetails.getUsername())
                    .or(i -> i.eq("receiver_username", username)
                            .eq("sender_username", userDetails.getUsername())));
        } else {
            list = sendMsgService.list(new QueryWrapper<SendMsg>().like("message", searchName)
                    .and(i -> i.eq("sender_username", username).eq("receiver_username", userDetails.getUsername())
                            .or(j -> j.eq("receiver_username", username)
                                    .eq("sender_username", userDetails.getUsername()))));
        }
        DetailUser detailUser = detailUserService.getOne(new QueryWrapper<DetailUser>().eq("username", username));
        if (list.size() == 0) {
            model.addAttribute("message", "暂无信息！");
        } else {
            model.addAttribute("message", "信息如下：");
            model.addAttribute("list", list);
        }
        List<Scale> scaleList = scaleService.list();
        model.addAttribute("scaleList", scaleList);
        model.addAttribute("detailUser", detailUser);
        model.addAttribute("searchName", searchName);
        return "appoint_deal";
    }
}
