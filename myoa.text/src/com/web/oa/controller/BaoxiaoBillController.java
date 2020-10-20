package com.web.oa.controller;

import com.web.oa.pojo.ActiveUser;
import com.web.oa.pojo.BaoxiaoBill;
import com.web.oa.service.impl.BaoxiaoServiceImpl;
import com.web.oa.service.impl.WorkFlowServiceImpl;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class BaoxiaoBillController {

    @Autowired
    private BaoxiaoServiceImpl baoxiaoService;
    @Autowired
    private WorkFlowServiceImpl workFlowService;

    @RequestMapping(value = "/main")
    public String main(Model model){
        ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
        model.addAttribute("activeUser",activeUser);
        System.out.println("此次登录 办公系统 的是:"+activeUser.getUsername());
        return "index";
    }

    @RequestMapping(value = "/myBaoxiaoBill")
    public ModelAndView myBaoxiaoBill(){
        ModelAndView modelAndView=new ModelAndView();
        ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
        List<BaoxiaoBill> leaveBillList = baoxiaoService.findLeaveBillListByUser(activeUser.getId());
        modelAndView.addObject("baoxiaoList",leaveBillList);
        modelAndView.setViewName("baoxiaobill");
        return modelAndView;
    }

}
