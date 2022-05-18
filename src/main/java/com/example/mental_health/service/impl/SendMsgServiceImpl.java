package com.example.mental_health.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mental_health.pojo.SendMsg;
import com.example.mental_health.service.SendMsgService;
import com.example.mental_health.mapper.SendMsgMapper;
import org.springframework.stereotype.Service;

/**
* @author ZST-PC
* @description 针对表【tb_send_msg】的数据库操作Service实现
* @createDate 2022-04-07 10:08:16
*/
@Service
public class SendMsgServiceImpl extends ServiceImpl<SendMsgMapper, SendMsg>
    implements SendMsgService{

}




