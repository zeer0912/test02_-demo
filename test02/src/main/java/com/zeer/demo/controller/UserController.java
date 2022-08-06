package com.zeer.demo.controller;

import com.zeer.demo.annotation.PassLogin;
import com.zeer.demo.bean.User;
import com.zeer.demo.service.mpl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Pattern;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
@Validated
@RestController
public class UserController {
    @Autowired
    UserServiceImpl userService;

    public static boolean rexCheckPassword(String input) {
        // 6-20 位，字母、数字、字符
        String regStr = "^([A-Z]|[a-z]|[0-9]|[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“'。，、？]){8,20}$";
        return input.matches(regStr);
    }

    @PostMapping("/changePa")
    public Map<String,Object> changePa(
            String password,
            String newPassword, HttpServletRequest request) {
        Long userId = Long.parseLong(request.getAttribute("subject").toString());
        Map<String,Object> map= new HashMap<>();
        User query = userService.getByPa(userId, password);
        log.info("成功查询");
        System.out.println(query);
        if (StringUtils.isEmpty(password) || StringUtils.isEmpty(newPassword)) {
            map.put("msg", "密码不能为空");
        } else if (query == null) {

            map.put("msg", "旧密码错误");

        } else if (rexCheckPassword(newPassword) == false) {
            map.put("msg", "密码规定为 8-20 位的字母、数字、字符");
        } else {
            userService.upDatePa(userId,newPassword);
            map.put("msg", "密码修改成功");
            map.put("code", "200");


        }

        return map;

    }
    @PassLogin
    @GetMapping(value = {"/ChangeSendText"})
    public Map<String,Object> ChangeSendText(){

        Map<String,Object> map=new HashMap<>();
        map.put("msg", "修改密码的验证码获取成功");
        map.put("code", 200);
        map.put("text","123456");
        return map;
    }

    @PassLogin
    @PostMapping("/changePaByText")
    public Map<String,Object> changePaByText(@Pattern(regexp = "^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$",message = "手机号格式有误") String phoneNumber,
                                        String newPassword, String text,HttpServletRequest request) {

        Map<String,Object> map= new HashMap<>();
        User query = userService.getByPhoneNumber(phoneNumber);
        log.info("成功查询");
        System.out.println(query);
        if (query == null) {
            map.put("msg", "手机号未注册");
        } else if(!text.equals("123456"))
        {
            map.put("msg","验证码错误");
        } else if (rexCheckPassword(newPassword) == false) {
            map.put("msg", "密码规定为 8-20 位的字母、数字、字符");
        } else {
            User user=userService.getByPhoneNumber(phoneNumber);
            userService.upDatePa(user.getId(), newPassword);
            map.put("msg", "密码修改成功");
            map.put("code", "200");
        }

        return map;

    }

    @PostMapping("/selectAll")
    public Map<String,Object> selectAll(String name, String object, HttpServletRequest request) {
        Long userId = Long.parseLong(request.getAttribute("subject").toString());
        Map<String,Object> map= new HashMap<>();
        List<User> list=new LinkedList<>();
        switch (name){
            case "phoneNumber":list.add(userService.getByPhoneNumber(object));break;
            case "username":list= userService.getByUsername(object);break;
            case "studentID":list= userService.getByStudentID(object);break;
        }
        if(list.isEmpty()) map.put("msg","找不到符合条件的用户");
        else {
            map.put("msg", "查询成功");
            map.put("code", "200");
            map.put("list",list);
        }

        return map;

    }


}
