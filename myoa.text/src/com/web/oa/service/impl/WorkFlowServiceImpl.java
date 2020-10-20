package com.web.oa.service.impl;

import com.web.oa.mapper.BaoxiaoBillMapper;
import com.web.oa.pojo.BaoxiaoBill;
import com.web.oa.service.WorkFlowService;
import com.web.oa.utils.Constants;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.explorer.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

@Service
public class WorkFlowServiceImpl implements WorkFlowService {

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private BaoxiaoBillMapper baoxiaoBillMapper;

    /**
     * 创建新的流程
     * @param in
     * @param filename
     */
    @Override
    public void saveNewDeploye(InputStream in, String filename) {
        ZipInputStream zipInputStream=new ZipInputStream(in);
        repositoryService.createDeployment()
                .name(filename)
                .addZipInputStream(zipInputStream)
                .deploy();
    }

    @Override
    public List<Deployment> findDeploymentList() {
        List<Deployment> list = repositoryService.createDeploymentQuery()
                .orderByDeploymenTime()
                .asc()
                .list();
        return list;
    }

    @Override
    public List<ProcessDefinition> findProcessDefinitionList() {
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery()
                .orderByProcessDefinitionVersion().asc()
                .list();
        return list;
    }

    @Override
    public void saveStartProcess(long baoxiaoId, String username) {
        String baoxiaoKey = Constants.BAOXIAO_KEY;
        Map<String,Object> map=new HashMap<>();
        map.put("inputUser",username);
        String objId= baoxiaoKey+"."+baoxiaoId;
        map.put("objId",objId);
        runtimeService.startProcessInstanceByKey(baoxiaoKey,objId,map);

    }

    @Override
    public List<Task> findTaskListByName(String name) {
        List<Task> list = taskService.createTaskQuery()
                .taskAssignee(name)
                .orderByTaskCreateTime()
                .asc().list();
        return list;
    }

    @Override
    public BaoxiaoBill findBaoxiaoBillByTaskId(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId()).singleResult();
        String businessKey = processInstance.getBusinessKey();
        String id="";
        if(StringUtils.isNotBlank(businessKey)){
            id=businessKey.split("\\.")[1];
        }
        BaoxiaoBill baoxiaoBill = baoxiaoBillMapper.selectByPrimaryKey(Long.parseLong(id));
        return baoxiaoBill;
    }

    @Override
    public List<Comment> findCommentByTaskId(String taskId) {
        String processInstanceId = taskService.createTaskQuery().taskId(taskId).singleResult().getProcessInstanceId();
        List<Comment> comments = taskService.getProcessInstanceComments(processInstanceId);
        return comments;
    }

    @Override
    public List<String> findOutComeListByTaskId(String taskId) {
        List<String> list=new ArrayList<>();
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String processDefinitionId = task.getProcessDefinitionId();
        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(processDefinitionId);
        String processInstanceId = task.getProcessInstanceId();
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        String activityId = processInstance.getActivityId();
        ActivityImpl activity = processDefinition.findActivity(activityId);
        List<PvmTransition> pvmList = activity.getOutgoingTransitions();
        if(null!=pvmList&&pvmList.size()>0){
            for (PvmTransition transition : pvmList) {
                String name = (String) transition.getProperty("name");
                if(StringUtils.isNotBlank(name)){
                    list.add(name);
                }else {
                    list.add("默认提交");
                }
            }
        }
        return list;
    }

    @Override
    public void saveSubmitTask(long id, String taskId, String comemnt, String outcome, String username) {
        String processInstanceId = taskService.createTaskQuery().taskId(taskId).singleResult().getProcessInstanceId();
        Authentication.setAuthenticatedUserId(username);
        taskService.addComment(taskId,processInstanceId,comemnt);
        Map<String,Object> map=new HashMap<>();
        if(null!=outcome&&!outcome.equals(Constants.CONSTANTS_MOREN)){
            map.put("message",outcome);
            taskService.complete(taskId,map);
        }else {
            taskService.complete(taskId);
        }
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        if(null==processInstance){
            BaoxiaoBill baoxiaoBill = baoxiaoBillMapper.selectByPrimaryKey(id);
            baoxiaoBill.setState(2);
            baoxiaoBillMapper.updateByPrimaryKey(baoxiaoBill);
        }

    }

    @Override
    public Map<String,Object> findProcessDefinitionByTaskId(String taskId) {
        Map<String,Object> map=new HashMap<>();
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();

        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity)
                repositoryService.getProcessDefinition(task.getProcessDefinitionId());
        map.put("processDefinition",processDefinition);
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .singleResult();
        ActivityImpl activity = processDefinition.findActivity(processInstance.getActivityId());
        map.put("activity",activity);
        return map;
    }

    @Override
    public Map<String, Object> findCoordingByTask(String taskId) {
        return null;
    }

    @Override
    public InputStream findImageInputStream(String deploymentId, String imageName) {
        InputStream inputStream = repositoryService
                .getResourceAsStream(deploymentId, imageName);
        return inputStream;
    }

    @Override
    public Task findTaskByBussinessKey(String bUSSINESS_KEY) {
        Task task = taskService.createTaskQuery()
                .processInstanceBusinessKey(bUSSINESS_KEY)
                .singleResult();
        return task;
    }

    @Override
    public List<Comment> findCommentByBaoxiaoBillId(long id) {
        return null;
    }

    @Override
    public void deleteProcessDefinitionByDeploymentId(String deploymentId) {
        repositoryService.deleteDeployment(deploymentId,true);
    }
}
