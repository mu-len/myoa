package com.web.oa.service.impl;

import com.web.oa.mapper.*;
import com.web.oa.pojo.*;
import com.web.oa.service.SysService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SysServiceImpl implements SysService {

    @Autowired
    private SysPermissionMapperCustom sysPermissionMapperCustom;
    @Autowired
    private SysRoleMapper sysRoleMapper;
    @Autowired
    private SysRolePermissionMapper sysRolePermissionMapper;
    @Autowired
    private SysPermissionMapper sysPermissionMapper;


    @Override
    public List<SysPermission> findMenuListByUserId(String userid) throws Exception {
        return sysPermissionMapperCustom.findMenuListByUserId(userid);
    }

    @Override
    public List<SysPermission> findPermissionListByUserId(String userid) throws Exception {
        return sysPermissionMapperCustom.findPermissionListByUserId(userid);
    }

    @Override
    public List<MenuTree> loadMenuTree() {
        return sysPermissionMapperCustom.getMenuTree();
    }

    @Override
    public List<SysRole> findAllRoles() {
        return sysRoleMapper.selectByExample(null);
    }

    @Override
    public SysRole findRolesAndPermissionsByUserId(String userId) {
        return sysPermissionMapperCustom.findRoleAndPermissionListByUserId(userId);
    }

    @Override
    public void addRoleAndPermissions(SysRole role, int[] permissionIds) {
        for(int i=0;i<permissionIds.length;i++){
            SysRolePermission sysRolePermission=new SysRolePermission();
            String id = UUID.randomUUID().toString();
            sysRolePermission.setId(id);
            sysRolePermission.setSysRoleId(role.getId());
            sysRolePermission.setSysPermissionId(permissionIds[i]+"");
            sysRolePermissionMapper.insert(sysRolePermission);
        }
        sysRoleMapper.insert(role);

    }

    @Override
    public List<SysPermission> findAllMenus() {
        SysPermissionExample sysPermissionExample=new SysPermissionExample();
        SysPermissionExample.Criteria criteria = sysPermissionExample.createCriteria();
        criteria.andTypeEqualTo("menu");
        return sysPermissionMapper.selectByExample(sysPermissionExample);
    }

    @Override
    public void addSysPermission(SysPermission permission) {
        sysPermissionMapper.insert(permission);
    }

    @Override
    public List<SysPermission> findMenuAndPermissionByUserId(String userId) {
        return sysPermissionMapperCustom.findMenuAndPermissionByUserId(userId);
    }

    @Override
    public List<MenuTree> getAllMenuAndPermision() {
        return sysPermissionMapperCustom.getAllMenuAndPermision();
    }

    @Override
    public List<SysPermission> findPermissionsByRoleId(String roleId) {
        return sysPermissionMapperCustom.findPermissionsByRoleId(roleId);
    }

    @Override
    public void updateRoleAndPermissions(String roleId, int[] permissionIds) {
        SysRolePermissionExample sysRolePermissionExample=new SysRolePermissionExample();
        SysRolePermissionExample.Criteria criteria = sysRolePermissionExample.createCriteria();
        criteria.andSysRoleIdEqualTo(roleId);
        sysRolePermissionMapper.deleteByExample(sysRolePermissionExample);
        for (Integer permissionId : permissionIds) {
            SysRolePermission sysRolePermission=new SysRolePermission();
            String uuid = UUID.randomUUID().toString();
            sysRolePermission.setId(uuid);
            sysRolePermission.setSysRoleId(roleId);
            sysRolePermission.setSysPermissionId(permissionId.toString());
            sysRolePermissionMapper.insert(sysRolePermission);
        }
    }

    @Override
    public List<SysRole> findRolesAndPermissions() {
        return sysPermissionMapperCustom.findRoleAndPermissionList();
    }

    @Override
    public void delSysRole(String id){
        sysRoleMapper.deleteByPrimaryKey(id);
    }
}
