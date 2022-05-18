package com.example.mental_health.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mental_health.pojo.Question;
import com.example.mental_health.service.QuestionService;
import com.example.mental_health.mapper.QuestionMapper;
import org.springframework.stereotype.Service;

/**
* @author ZST-PC
* @description 针对表【tb_question】的数据库操作Service实现
* @createDate 2022-04-09 11:54:41
*/
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
    implements QuestionService{

}




