package com.web.oa.advice;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;;
import org.springframework.stereotype.Component;

@Component(value = "advice")
@Aspect
public class MyAdvice {

    @Around(value = "execution(* com.web.oa.mapper.*Mapper.select*(..))")
    public Object aroundSelect(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("==============================================================================================\n开始执行数据查询。。。。");
        long start=System.currentTimeMillis();
        Object proceed = pjp.proceed();
        long end=System.currentTimeMillis()-start;
        System.out.println("查询完成,开始时间为:"+start+",花费时间:"+end);
        return proceed;
    }

    @Around(value = "execution(* com.web.oa.mapper.*Mapper.update*(..))")
    public Object aroundUpdate(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("==============================================================================================\n开始执行数据更新。。。。");
        long start=System.currentTimeMillis();
        Object proceed = pjp.proceed();
        long end=System.currentTimeMillis()-start;
        System.out.println("更新完成,开始时间为:"+start+",花费时间:"+end);
        return proceed;
    }

    @Around(value = "execution(* com.web.oa.mapper.*Mapper.insert*(..))")
    public Object aroundInsert(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("==============================================================================================\n开始执行数据插入。。。。");
        long start=System.currentTimeMillis();
        Object proceed = pjp.proceed();
        long end=System.currentTimeMillis()-start;
        System.out.println("插入完成,开始时间为"+start+",花费时间:"+end);
        return proceed;
    }

    @Around(value = "execution(* com.web.oa.mapper.*Mapper.delete*(..))")
    public Object aroundDelete(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("==============================================================================================\n开始执行数据删除。。。。");
        long start=System.currentTimeMillis();
        Object proceed = pjp.proceed();
        long end=System.currentTimeMillis()-start;
        System.out.println("删除完成,开始时间为:"+start+",花费时间:"+end);
        return proceed;
    }

    @AfterThrowing(value = "execution(* com.web.oa.mapper.*Mapper.*(..))")
    public void afterException(){
        System.out.println("出现异常！！！！");
    }

    @After(value = "execution(* com.web.oa.mapper.*Mapper.*(..))")
    public void after(){
        System.out.println("任务执行结束。");
    }
}
