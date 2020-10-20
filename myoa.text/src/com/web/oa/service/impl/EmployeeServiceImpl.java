package com.web.oa.service.impl;

import com.web.oa.exception.MyException;
import com.web.oa.mapper.EmployeeMapper;
import com.web.oa.mapper.SysPermissionMapperCustom;
import com.web.oa.mapper.SysUserRoleMapper;
import com.web.oa.pojo.*;
import com.web.oa.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;
    @Autowired
    private SysPermissionMapperCustom permissionMapperCustom;
    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Override
    public Employee findEmployeeByName(String name) {
        EmployeeExample employeeExample=new EmployeeExample();
        EmployeeExample.Criteria criteria = employeeExample.createCriteria();
        criteria.andNameEqualTo(name);
        List<Employee> employees = employeeMapper.selectByExample(employeeExample);
        if(null!=employees&&employees.size()>0){
            return employees.get(0);
        }
        return null;
    }

    @Override
    public Employee findEmployeeManager(String id) {
        return employeeMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<Employee> findUsers() {
        return employeeMapper.selectByExample(null);
    }

    @Override
    public List<EmployeeCustom> findUserAndRoleList() {
        return permissionMapperCustom.findUserAndRoleList();
    }

    @Override
    public void updateEmployeeRole(String roleId, String userId) {
        SysUserRoleExample sysUserRoleExample=new SysUserRoleExample();
        SysUserRoleExample.Criteria criteria = sysUserRoleExample.createCriteria();
        criteria.andSysUserIdEqualTo(userId);
        List<SysUserRole> sysUserRoles = sysUserRoleMapper.selectByExample(sysUserRoleExample);
        SysUserRole sysUserRole = sysUserRoles.get(0);
        sysUserRole.setSysRoleId(roleId);
        sysUserRoleMapper.updateByPrimaryKey(sysUserRole);
    }

    @Override
    public List<Employee> findEmployeeByLevel(int level) {
        EmployeeExample employeeExample=new EmployeeExample();
        EmployeeExample.Criteria criteria = employeeExample.createCriteria();
        criteria.andRoleEqualTo(level);
        List<Employee> employees = employeeMapper.selectByExample(employeeExample);

        return employees;
    }

    @Override
    public void insertEmployee(Employee employee) throws MyException {
        int insert=0;
        try{
            insert = employeeMapper.insert(employee);
        }catch (Exception e){
            throw new MyException("账号或者邮箱已经被注册");
        }

        if(insert>0){
            SysUserRole sysUserRole=new SysUserRole(employee.getId().toString(),employee.getName(),employee.getRole());
            sysUserRoleMapper.insert(sysUserRole);
        }
    }
}
