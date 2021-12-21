package com.yi.auth.service;

import com.yi.auth.vo.UserInfoVo;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private Integer getUserId(String token){
        // TODO 自行实现
        return 12138;
    }

    private UserInfoVo getUserInfo(String token){
        Integer userId = getUserId(token);
        UserInfoVo infoVo = new UserInfoVo();
        infoVo.setUserId(userId);
        infoVo.setUserName(userId + "_chinese");
        // TODO 自行实现
        return infoVo;
    }
}
