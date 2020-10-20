package com.web.oa.shiro;

import com.web.oa.pojo.ActiveUser;
import com.web.oa.pojo.Employee;
import com.web.oa.pojo.MenuTree;
import com.web.oa.pojo.SysPermission;
import com.web.oa.service.impl.EmployeeServiceImpl;
import com.web.oa.service.impl.SysServiceImpl;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class EmployeeRealm extends AuthorizingRealm {

    @Autowired
    private EmployeeServiceImpl employeeService;
    @Autowired
    private SysServiceImpl sysService;

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        ActiveUser activeUser = (ActiveUser) principalCollection.getPrimaryPrincipal();
        List<SysPermission> listByUserId=new ArrayList<>();
        try {
            listByUserId = sysService.findPermissionListByUserId(activeUser.getUserid());
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<String> list=new ArrayList<>();
        for (SysPermission sysPermission : listByUserId) {
            list.add(sysPermission.getPercode());
        }
        SimpleAuthorizationInfo info=new SimpleAuthorizationInfo();
        info.addStringPermissions(list);
        System.out.println("当前员工持有的访问权限:\n"+list);
        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        String name = authenticationToken.getPrincipal().toString();
        Employee employee = employeeService.findEmployeeByName(name);
        if(null==employee){
            return null;
        }
        List<MenuTree> menuTrees = sysService.loadMenuTree();
        ActiveUser activeUser=new ActiveUser();
        activeUser.setId(employee.getId());
        activeUser.setUsername(employee.getName());
        activeUser.setUsercode(employee.getName());
        activeUser.setUserid(employee.getName());
        activeUser.setManagerId(employee.getManagerId()==null?1:Long.parseLong(employee.getManagerId()));
        activeUser.setMenuTree(menuTrees);
        SimpleAuthenticationInfo info=new SimpleAuthenticationInfo(activeUser,employee.getPassword(), ByteSource.Util.bytes("eteokues"),this.getName());
        return info;
    }
}
