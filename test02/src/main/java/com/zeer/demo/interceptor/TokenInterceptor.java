package com.zeer.demo.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.zeer.demo.annotation.PassLogin;
//import com.zeer.demo.http.ResponseResult;
import com.zeer.demo.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class TokenInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    @Autowired
    private JwtUtil jwtUtil;
    //在业务处理请求之前处理
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //设置response响应数据类型为json和编码为utf-8
        response.setContentType("application/json;charset=utf-8");
        // 判断对象是否是映射到一个方法，如果不是则直接通过
        if (!(handler instanceof HandlerMethod)) {
            // instanceof运算符是用来在运行时指出对象是否是特定类的一个实例
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;

        Method method = handlerMethod.getMethod();
        //检查方法是否有PassLogin注解，有则跳过认证
        boolean passLogin=method.isAnnotationPresent(PassLogin.class);
//        if (method.isAnnotationPresent(PassLogin.class)){
//
//            return true;
//        }

        // 从HTTP请求头中获取Authorization信息，
        String authorization = request.getHeader(jwtUtil.getHeader());
        //判断Authorization是否为空
        if(StringUtils.isEmpty(authorization)){
            if(passLogin) return true;
            logger.info("token无效1");
            response.getWriter().write("token无效");
//            model.addAttribute("msg","token无效");
//            response.getWriter().write(JSONObject.toJSONString(ResponseResult.unauthorized()));
            return false;
        }
        //获取TOKEN,注意要清除前缀"Bearer "
        String token = authorization.replace("Bearer ","");
        System.out.println(token);

        // HTTP请求头中TOKEN解析出的用户信息
        Claims claims = null;
        try{
            claims = jwtUtil.parseToken(token);
        }catch (Exception e){
            response.getWriter().write("token无效");
            return false;

        }

        System.out.println(claims);
        if(claims == null){
            if(passLogin) return true;
            logger.info("token无效2");
            response.getWriter().write("token无效");
//            model.addAttribute("msg","token无效");
//            response.getWriter().write(JSONObject.toJSONString(ResponseResult.unauthorized()));
            return false;
        }

        //token正常，获取用户信息，比如这里的subject存的是用户id
        String subject = claims.getSubject();
        //将用户信息存入request，以便后面处理请求使用
        request.setAttribute("subject",subject);

        //校验是否过期
        boolean flag = jwtUtil.isExpired(claims.getExpiration());
        if(flag){
            if(passLogin) return true;
            logger.error("token过期");
            response.getWriter().write("token过期\n");
            response.getWriter().write("token："+token);
//            response.getWriter().write(JSONObject.toJSONString(ResponseResult.unauthorized()));
            return false;
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }

}
