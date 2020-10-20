package com.web.oa.interceptor;

import org.apache.shiro.SecurityUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MyInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        String url = request.getRequestURI().substring(request.getRequestURI().lastIndexOf("/") + 1);
        String suffix=url.substring(url.lastIndexOf(".")+1);
        Object principal = SecurityUtils.getSubject().getPrincipal();
        if(null==principal&&!url.equals("login")&&!suffix.equals("css")&&!suffix.equals("js")){
            request.getRequestDispatcher("login.jsp").forward(request,response);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
