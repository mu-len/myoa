package com.web.oa.utils;

import com.web.oa.pojo.ActiveUser;
import com.web.oa.pojo.Employee;
import com.web.oa.service.impl.EmployeeServiceImpl;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.apache.shiro.SecurityUtils;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

public class ManagerTaskHandler implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
        EmployeeServiceImpl employeeService = (EmployeeServiceImpl) context.getBean("employeeServiceImpl");

        ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
        Employee emp = employeeService.findEmployeeManager(activeUser.getManagerId().toString());

        delegateTask.setAssignee(emp.getName());
    }
}
