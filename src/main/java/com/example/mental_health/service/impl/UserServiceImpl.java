package com.example.mental_health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mental_health.mapper.UserMapper;
import com.example.mental_health.pojo.User;
import com.example.mental_health.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;


@Service("userDetailsService")
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService, UserDetailsService {

    @Autowired
    private UserMapper mapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //调用usersMapper方法，根据用户名查询数据库
        QueryWrapper<User> wrapper = new QueryWrapper();
        // where username=?
        wrapper.eq("username",username);
        User user = mapper.selectOne(wrapper);
        //判断
        if(user == null) {//数据库没有用户名，认证失败
            throw new UsernameNotFoundException("用户名不存在");
        }

        List<GrantedAuthority> auths = AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_" + user.getRole());

        //从查询数据库返回users对象，得到用户名和密码，返回
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), auths);
    }
}




