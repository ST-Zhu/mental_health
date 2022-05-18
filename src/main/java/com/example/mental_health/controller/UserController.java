package com.example.mental_health.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.mental_health.exception.CustomException;
import com.example.mental_health.pojo.*;
import com.example.mental_health.pojo.vo.GradeLevel;
import com.example.mental_health.result.Result;
import com.example.mental_health.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import javax.annotation.PostConstruct;
import java.util.*;


@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    DetailUserService detailUserService;

    @Autowired
    SendMsgService sendMsgService;

    @Autowired
    AppointService appointService;

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
    public String index(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, GradeLevel> map = new HashMap<>();
        List<Scale> list = scaleService.list();
        for (Scale scale : list) {
            int full = scale.getScoreFull();
            long count1 = userScaleService.count(new QueryWrapper<UserScale>().eq("scale_name", scale.getName())
                    .between("grade", 0, full/3).eq("user_username", userDetails.getUsername()));
            long count2 = userScaleService.count(new QueryWrapper<UserScale>().eq("scale_name", scale.getName())
                    .between("grade", full/3, (full/3)*2).eq("user_username", userDetails.getUsername()));
            long count3 = userScaleService.count(new QueryWrapper<UserScale>().eq("scale_name", scale.getName())
                    .between("grade", (full/3)*2, full).eq("user_username", userDetails.getUsername()));

            map.put(scale.getName(), new GradeLevel(count1, count2, count3));
        }
        model.addAttribute("map", map);
        return "index_user";
    }

    @GetMapping("/healthTest")
    public String healthTest(Model model) {
        return queryScale(model, null);
    }

    @GetMapping("/historyRecord")
    public String historyRecord(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        return queryHistory(model, null, userDetails);
    }

    @GetMapping("/queryDoctor")
    public String queryDoctor(String searchName, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        List<DetailUser> list;
        if ((searchName == null) || (searchName.isEmpty())) {
            list = detailUserService.list(new QueryWrapper<DetailUser>().eq("role", "doctor"));
        } else{
            list = detailUserService.list(new QueryWrapper<DetailUser>()
                    .eq("role", "doctor").like("name", searchName));
        }

        List<DetailUser> consultList = new ArrayList<>();
        List<DetailUser> appointList = new ArrayList<>();
        List<DetailUser> doctorList = new ArrayList<>();
        for (DetailUser detailUser : list) {
            long count = appointService.count(new QueryWrapper<Appoint>().eq("doctor_username", detailUser.getUsername())
                    .eq("state", "预约中"));
            long count2 = appointService.count(new QueryWrapper<Appoint>().eq("doctor_username", detailUser.getUsername())
                    .eq("state", "完成"));
            detailUser.setAppointNum(count);
            detailUser.setCompleteNum(count2);
            detailUserService.updateById(detailUser);

            long consultCount = appointService.count(new QueryWrapper<Appoint>().eq("doctor_username", detailUser.getUsername())
                    .eq("user_username", userDetails.getUsername()).eq("state", "咨询中"));
            long appointCount = appointService.count(new QueryWrapper<Appoint>().eq("doctor_username", detailUser.getUsername())
                    .eq("user_username", userDetails.getUsername()).eq("state", "预约中"));
            if (consultCount > 0) {
                consultList.add(detailUser);
            } else if (appointCount > 0) {
                appointList.add(detailUser);
            } else{
                doctorList.add(detailUser);
            }


        }

        model.addAttribute("searchName", searchName);
        if ((consultList.size() + appointList.size() + doctorList.size()) == 0) {
            model.addAttribute("message", "暂无医师信息！");
        } else {
            model.addAttribute("message", "医师信息如下：");
            model.addAttribute("consultList", consultList);
            model.addAttribute("appointList", appointList);
            model.addAttribute("doctorList", doctorList);
        }
        return "appointServe_user";
    }

    @PostMapping("/appoint")
    @ResponseBody
    public String appoint(@AuthenticationPrincipal UserDetails userDetails,
                          String username, String sendMsg) {
        if ((username == null) || (username.isEmpty())) {
            throw new CustomException("用户名不能为空");
        }
        long size = appointService.count(new QueryWrapper<Appoint>().eq("doctor_username", username)
                .eq("user_username", userDetails.getUsername()).ne("state", "完成"));
        if (size != 0) {
            throw new CustomException("不可重复预约同一个医生");
        }

        Appoint appoint = new Appoint();
        appoint.setDoctorUsername(username);
        appoint.setUserUsername(userDetails.getUsername());
        appoint.setCreateTime(new Date());
        appoint.setState("预约中");
        boolean save = appointService.save(appoint);
        if (!save) {
            throw new CustomException("预约失败");
        }

        if ((sendMsg == null) || (sendMsg.isEmpty())) {
            throw new CustomException("发送信息真的不能为空");
        }
        save = sendMsgService.save(new SendMsg(null, userDetails.getUsername(), username, sendMsg, null, new Date(), null, null, null));
        if (!save) {
            throw new CustomException("预约失败");
        }
        return JSON.toJSONString(Result.buildSuccess("预约成功"));
    }

    @GetMapping("/personalCenter")
    public String personalCenter(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        DetailUser detailUser = detailUserService.getOne(new QueryWrapper<DetailUser>().eq("username", userDetails.getUsername()));
        model.addAttribute("detailUser", detailUser);
        return "detail_user";
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
        return "account_user";
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
        return "scale_user";
    }

    @GetMapping("scaleText")
    public String scaleText(Model model, String id) {
        Scale scale = scaleService.getById(id);
        List<Question> list = questionService.list(new QueryWrapper<Question>().eq("scale_name", scale.getName()));
        if (list.size() == 0) {
            model.addAttribute("message", "暂无信息！");
        } else {
            model.addAttribute("message", "信息如下：");
            model.addAttribute("list", list);
        }
        model.addAttribute("scale", scale);
        return "question_user";
    }

    // TODO 计算试卷的分数，前端不会，采用后端处理，待改进
    @ResponseBody
    @PostMapping("score")
    public String score(@RequestParam Map<String, String> map, @AuthenticationPrincipal UserDetails userDetails){
        int grade = 0;
        String scaleName = map.get("scaleName");
        map.remove("scaleName");
        String standard = map.get("standard");
        map.remove("standard");
        String scoreProportion = map.get("scoreProportion");
        map.remove("scoreProportion");
        for (Map.Entry<String, String> element : map.entrySet()) {
            grade += Integer.valueOf(element.getValue());
        }
        grade = (int)(grade * Float.parseFloat(scoreProportion));
        userScaleService.save(new UserScale(null, null, userDetails.getUsername(),
                scaleName, grade, standard, null, null, null, new Date()));
        return JSON.toJSONString(Result.buildSuccess("您好，您的测试分数为：" + grade));
    }

    @GetMapping("queryHistory")
    private String queryHistory(Model model, String searchName,
                                @AuthenticationPrincipal UserDetails userDetails) {
        List<UserScale> list;
        if ((searchName == null) || (searchName.isEmpty())) {
            list = userScaleService.list(new QueryWrapper<UserScale>()
                    .eq("user_username", userDetails.getUsername()).orderByDesc("test_time"));
        } else {
            list = userScaleService.list(new QueryWrapper<UserScale>()
                    .eq("user_username", userDetails.getUsername())
                    .like("scale_name", searchName).orderByDesc("test_time"));
        }
        if (list.size() == 0) {
            model.addAttribute("message", "暂无信息！");
        } else {
            model.addAttribute("message", "信息如下：");
            model.addAttribute("list", list);
        }
        model.addAttribute("searchName", searchName);
        return "history_user";
    }

    @GetMapping("consult")
    public String consult(Model model, String username, @AuthenticationPrincipal UserDetails userDetails, String searchName) {
        List<SendMsg> list;
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
        model.addAttribute("detailUser", detailUser);
        model.addAttribute("searchName", searchName);
        return "appoint_consult";
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

    @GetMapping("removeAppoint")
    public String removeAppoint(Model model, @AuthenticationPrincipal UserDetails userDetails, String username) {
        appointService.remove(new QueryWrapper<Appoint>().eq("doctor_username", username));
        return queryDoctor(null, model, userDetails);
    }

    @PostMapping("completeAppoint")
    @ResponseBody
    public String completeAppoint(String username, @AuthenticationPrincipal UserDetails userDetails) {
        boolean b = appointService.update(new UpdateWrapper<Appoint>().set("state", "完成").set("complete_time", new Date())
                .eq("doctor_username", username)
                .eq("user_username", userDetails.getUsername()).eq("state", "咨询中"));
        if (!b) {
            throw new CustomException("操作失败");
        }
        return JSON.toJSONString(Result.buildSuccess("操作成功"));
    }
}
