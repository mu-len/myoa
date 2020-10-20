package com.web.oa.controller;

import com.web.oa.exception.MyException;
import com.web.oa.pojo.*;
import com.web.oa.service.impl.EmployeeServiceImpl;
import com.web.oa.service.impl.SysServiceImpl;
import com.web.oa.utils.Constants;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class EmployeeController {

    @Autowired
    private EmployeeServiceImpl employeeService;
    @Autowired
    private SysServiceImpl sysService;

    @RequestMapping("/login")
    public String login(Model model,HttpServletRequest request){
        String shiroLoginFailure = (String) request.getAttribute("shiroLoginFailure");
        if(null!=shiroLoginFailure){
            if(UnknownAccountException.class.getName().equals(shiroLoginFailure)){
                model.addAttribute("errorMsg","账号不存在 ");
            }else if(IncorrectCredentialsException.class.getName().equals(shiroLoginFailure)){
                model.addAttribute("errorMsg","密码错误");
            }else if("CodeException".equals(shiroLoginFailure)){
                model.addAttribute("errorMsg","验证码错误");
            }else{
                model.addAttribute("errorMsg","未知错误");
            }
        }
        return "login";
    }

    @RequestMapping(value = "/findUserList")
    public ModelAndView findUserList(){
        ModelAndView modelAndView=new ModelAndView();
        List<EmployeeCustom> users = employeeService.findUserAndRoleList();
        List<SysRole> allRoles = sysService.findAllRoles();
        modelAndView.addObject("userList",users);
        modelAndView.addObject("allRoles",allRoles);
        modelAndView.setViewName("userlist");
        return modelAndView;
    }

    @RequestMapping(value = "/toAddRole")
    public ModelAndView toAddRole(HttpServletRequest request){
        Object massage = request.getAttribute("massage");
        ModelAndView modelAndView=new ModelAndView();
        List<SysRole> rolesAndPermissions = sysService.findRolesAndPermissions();
        List<SysPermission> allMenus = sysService.findAllMenus();
        List<MenuTree> menuTrees = sysService.loadMenuTree();
        if(null!=massage){
            modelAndView.addObject("massage",(String)massage);
        }
        modelAndView.addObject("allPermissions",menuTrees);
        modelAndView.addObject("menuTypes",allMenus);
        modelAndView.addObject("roleAndPermissionsList",rolesAndPermissions);

        modelAndView.setViewName("rolelist");
        return modelAndView;
    }

    @RequestMapping(value = "/findRoles")
    private ModelAndView findRoles(){
        ModelAndView modelAndView=new ModelAndView();
        List<SysRole> allRoles = sysService.findAllRoles();
        List<MenuTree> allMenuAndPermision = sysService.getAllMenuAndPermision();
        ActiveUser activeUser = (com.web.oa.pojo.ActiveUser) SecurityUtils.getSubject().getPrincipal();

        modelAndView.addObject("allRoles",allRoles);
        modelAndView.addObject("allMenuAndPermissions",allMenuAndPermision);
        modelAndView.addObject("activeUser",activeUser);
        modelAndView.setViewName("permissionlist");
        return modelAndView;
    }

    @RequestMapping(value = "/loadMyPermissions",method = RequestMethod.POST)
    @ResponseBody
    public List<SysPermission> loadMyPermissions(String roleId){
        List<SysPermission> list = sysService.findPermissionsByRoleId(roleId);
        return list;
    }

    @RequestMapping(value = "/updateRoleAndPermission")
    public String updateRoleAndPermission(String roleId,int[] permissionIds){
        if(null==permissionIds){
            return "redirect:/findRoles";
        }
        sysService.updateRoleAndPermissions(roleId,permissionIds);
        return "redirect:/findRoles";
    }

    @RequestMapping(value = "/saveRoleAndPermissions")
    public String saveRoleAndPermissions(SysRole sysRole,int[] permissionIds,HttpServletRequest request){
        sysRole.setId(UUID.randomUUID().toString());
        sysRole.setAvailable("1");
        try {
            sysService.addRoleAndPermissions(sysRole,permissionIds);
            request.setAttribute("massage","添加成功");
        }catch (Exception e){
            request.setAttribute("massage","添加失败");
        }
        return "forward:/toAddRole";
    }

    @RequestMapping(value = "/saveSubmitPermission")
    public String saveSubmitPermission(SysPermission sysPermission){
        if(null==sysPermission.getAvailable()){
            sysPermission.setAvailable("0");
        }
        sysService.addSysPermission(sysPermission);
        return "redirect:/toAddRole";
    }

    @RequestMapping(value = "/saveUser")
    public String saveUser(Employee employee){
        String salt= Constants.CONSTANTS_SALT;
        employee.setSalt(salt);
        Md5Hash md5Hash=new Md5Hash(employee.getPassword(),salt,2);
        employee.setPassword(md5Hash.toString());
        try {
            employeeService.insertEmployee(employee);
        } catch (MyException e) {
            e.printStackTrace();
        }
        return "redirect:/findUserList";
    }

    @RequestMapping(value = "/viewPermissionByUser")
    @ResponseBody
    public SysRole viewPermissionByUser(String userName){
        SysRole roles = sysService.findRolesAndPermissionsByUserId(userName);
        return roles;
    }

    @RequestMapping(value = "/findNextManager",method = RequestMethod.GET)
    @ResponseBody
    public List<Employee> findNextManager(int level){
        level++;
        List<Employee> employeeByLevel = employeeService.findEmployeeByLevel(level);
        if(employeeByLevel.size()==0){
            employeeByLevel.add(employeeService.findEmployeeManager("2"));
        }
        return employeeByLevel;
    }

    @RequestMapping(value = "/assignRole")
    @ResponseBody
    public Map<String,String> assignRole(String roleId,String userId){
        Map<String,String> map=new HashMap<>();
        try{
            employeeService.updateEmployeeRole(roleId,userId);
            map.put("msg","权限分配成功");
        }catch (Exception e){
            e.printStackTrace();
            map.put("msg","权限分配失败");
        }
        return map;
    }

    @RequestMapping(value = "/delSysRole",method = RequestMethod.GET)
    public String delSysRole(String id){
        sysService.delSysRole(id);
        return "redirect:/findRoles";
    }

    @RequestMapping(value = "/getLoginName",method = RequestMethod.GET)
    @ResponseBody
    public String getLoginName(){
        ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
        return activeUser.getUsername();
    }

    @RequestMapping(value = "/findRolesName")
    @ResponseBody
    public List<SysRole> findRolesName(){
        List<SysRole> allRoles = sysService.findAllRoles();
        return allRoles;
    }


}
