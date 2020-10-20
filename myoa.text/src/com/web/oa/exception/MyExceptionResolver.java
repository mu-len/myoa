package com.web.oa.exception;

import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MyExceptionResolver implements HandlerExceptionResolver {
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object o, Exception e) {
        String error="发生了未知异常";
        if(e instanceof MyException&&null!=((MyException) e).getMessage()){
            error=((MyException) e).getMassage();
        }
        ModelAndView modelAndView=new ModelAndView();
        modelAndView.addObject("massage","访问 "+request.getRequestURL()+" "+error);
        modelAndView.setViewName("error");
        return modelAndView;
    }
}
