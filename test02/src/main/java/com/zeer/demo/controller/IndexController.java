package com.zeer.demo.controller;



import com.zeer.demo.annotation.PassLogin;
import com.zeer.demo.bean.User;
import com.zeer.demo.service.mpl.UserServiceImpl;
import com.zeer.demo.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.TextUtils;


import javax.jws.soap.SOAPBinding;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

//@Slf4j
@RestController
@Validated
public class IndexController {


    @GetMapping(value = {"/","/login"})
    public String loginPage(){

        return "login";
    }
    @GetMapping(value = {"/register"})
    public String registerPage(){

        return "register";
    }
    @Autowired
    UserServiceImpl userService;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    @PassLogin
    public Map<String,Object> login(@Valid User user, HttpServletResponse response){ //RedirectAttributes

        User query=null;
        Map<String,Object> map=new HashMap<>();
        if(StringUtils.isEmpty(user.getPhoneNumber())||StringUtils.isEmpty(user.getPassword()))
        {
            map.put("msg","手机号和密码不能为空");
        }
        query=userService.getByPP(user.getPhoneNumber(), user.getPassword());
//        System.out.println(query);
        if(query!=null){
//            String token= CreateJwt.getToken(query);
            String token = jwtUtil.createToken(query.getId()+"");
            System.out.println(token);
            response.setHeader(jwtUtil.getHeader(),"Bearer "+token);
            map.put("token",token);
            map.put("user",query);
        }else {
            map.put("msg","账号密码错误");

        }
        return map;


    }

    @PostMapping("/loginByText")
    @PassLogin

    public Map<String,Object> loginByText(@Pattern(regexp = "^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$",message = "手机号格式有误")
                                          String phoneNumber,
                                          String text, HttpServletResponse response){
        User query=null;
        Map<String,Object> map=new HashMap<>();
        query=userService.getByPhoneNumber(phoneNumber);
//        System.out.println(query);
        if(query==null){
            map.put("msg","手机号未注册");
        }else if(!text.equals("123456"))
        {
            map.put("msg","验证码错误");
        }else {
            String token = jwtUtil.createToken(query.getId()+"");
            System.out.println(token);
            response.setHeader(jwtUtil.getHeader(),"Bearer "+token);
            map.put("token",token);
            map.put("user",query);
        }
        return map;


    }


    @PassLogin
    @GetMapping(value = {"/RegisterSendText"})
    public Map<String,Object> RegisterSendText(){

        Map<String,Object> map=new HashMap<>();
        map.put("msg", "注册验证码获取成功");
        map.put("code", 200);
        map.put("text","123456");
        return map;
    }

    @PassLogin
    @GetMapping(value = {"/LoginSendText"})
    public Map<String,Object> LoginSendText(){

        Map<String,Object> map=new HashMap<>();
        map.put("msg", "登录验证码获取成功");
        map.put("code", 200);
        map.put("text","123456");
        return map;
    }

    @PassLogin
    @PostMapping("/register")
    public Map<String,Object> register(@Valid User user, String text){

        Map<String,Object> map=new HashMap<>();
        User query=userService.getByPhoneNumber(user.getPhoneNumber());
        if(query!=null)
        {
            map.put("msg","该手机号已被注册");

        }else if(!text.equals("123456"))
        {
            map.put("msg","验证码错误");
        }else {
            userService.insert(user);
            map.put("msg", "注册成功");
            map.put("code", 200);
        }

        return map;
    }


    @GetMapping("/findUser")
    public Map<String, Object> findUser(HttpServletRequest request){
        Map<String, Object> map = new HashMap<>();
        //在拦截器中已经将从token中解析好的subject存入了request域，所以这里能获取到
        Long userId = Long.parseLong(request.getAttribute("subject").toString());
        User user= userService.getById(userId);
        //模拟返回一个数据
        map.put("user",user);
        map.put("msg", "成功");
        map.put("code", 200);
        return map;
    }

    @PassLogin
    @GetMapping("/refreshToken")
    public Map<String, Object> refreshToken(HttpServletRequest request,HttpServletResponse response){
        Map<String, Object> map = new HashMap<>();
            //在拦截器中已经将从token中解析好的subject存入了request域，所以这里能获取到
            Long userId = Long.parseLong(request.getAttribute("subject").toString());
            String token = jwtUtil.createToken(userId+"");
            response.setHeader(jwtUtil.getHeader(),"Bearer "+token);
            map.put("msg", "成功刷新token");
            map.put("code", 200);
            map.put("token",token);


        return map;
    }


    @GetMapping("/My.html")
    public String myPage(){

        return "My";

    }
}
