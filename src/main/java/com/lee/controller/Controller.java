package com.lee.controller;

import com.lee.annotation.GetParam;
import com.lee.dto.LoginDTO;
import com.lee.model.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


/**
 * @Auther: liyiyu
 * @Date: 2020/6/27 18:26
 * @Description:
 */
@RestController
public class Controller {



    @PostMapping("/login")
    public Object login(LoginDTO loginDTO){
        if ("lee".equals(loginDTO.getUsername()) && "123".equals(loginDTO.getPassword())){
            User user = User.builder()
                    .name("lee")
                    .sex(1)
                    .age(18)
                    .build();

            return user;
        }else {
            User user = User.builder().build();
            return user;
        }

    }


    @GetMapping("/get")
    public Object get(@GetParam("pageNum") String pageNum,
                      @GetParam("pageSize")String pageSize,
                      @GetParam("username")String username){
        Map<String,Object> map = new HashMap<>();
        map.put("pageNum",pageNum);
        map.put("pageSize",pageSize);
        map.put("username",username);
        return map;
    }




}
