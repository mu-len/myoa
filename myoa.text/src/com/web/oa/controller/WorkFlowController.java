package com.web.oa.controller;

import com.web.oa.pojo.ActiveUser;
import com.web.oa.pojo.BaoxiaoBill;
import com.web.oa.service.impl.BaoxiaoServiceImpl;
import com.web.oa.service.impl.WorkFlowServiceImpl;
import com.web.oa.utils.Constants;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
public class WorkFlowController {

    @Autowired
    private WorkFlowServiceImpl workFlowService;
    @Autowired
    private BaoxiaoServiceImpl baoxiaoService;

    @RequestMapping(value = "/myTaskList")
    public ModelAndView myTaskList() {
        ModelAndView modelAndView = new ModelAndView();
        ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
        List<Task> taskListByName = workFlowService.findTaskListByName(activeUser.getUsername());
        modelAndView.addObject("taskList", taskListByName);
        modelAndView.setViewName("workflow_task");
        return modelAndView;
    }

    @RequestMapping(value = "/processDefinitionList")
    public ModelAndView processDefinitionList() {
        ModelAndView modelAndView = new ModelAndView();
        List<Deployment> deploymentList = workFlowService.findDeploymentList();
        List<ProcessDefinition> processDefinitionList = workFlowService.findProcessDefinitionList();
        modelAndView.addObject("depList", deploymentList);
        modelAndView.addObject("pdList", processDefinitionList);
        modelAndView.setViewName("workflow_list");
        return modelAndView;
    }

    @RequestMapping(value = "/viewImage")
    public void viewImage(String deploymentId, String imageName, HttpServletResponse response) throws IOException {
        InputStream inputStream = workFlowService.findImageInputStream(deploymentId, imageName);
        ServletOutputStream outputStream = response.getOutputStream();
        IOUtils.copy(inputStream, outputStream);
    }

    @RequestMapping(value = "/deployProcess")
    public String deployProcess(String processName, MultipartFile fileName) {
        try {
            workFlowService.saveNewDeploye(fileName.getInputStream(), processName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "redirect:/processDefinitionList";
    }

    @RequestMapping(value = "/delDeployment")
    public String delDeployment(String deploymentId) {
        workFlowService.deleteProcessDefinitionByDeploymentId(deploymentId);
        return "redirect:/processDefinitionList";
    }

    @RequestMapping(value = "/saveStartBaoxiao")
    public String saveStartBaoxiao(BaoxiaoBill baoxiaoBill) {
        ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
        baoxiaoBill.setCreatdate(new Date());
        baoxiaoBill.setUserId(activeUser.getId());
        baoxiaoBill.setState(1);
        baoxiaoService.saveBaoxiao(baoxiaoBill);
        workFlowService.saveStartProcess(baoxiaoBill.getId(), activeUser.getUsername());
        return "redirect:/myTaskList";
    }

    @RequestMapping(value = {"/viewCurrentImage", "/viewCurrentImageByBill"})
    public ModelAndView viewCurrentImage(String taskId, String billId) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("viewimage");
        Map<String, Object> map = null;
        try {
            if (null != taskId) {//taskId不为null说明请求是/viewCurrentImage
                map = workFlowService.findProcessDefinitionByTaskId(taskId);
            } else {//taskId为null说明请求是/viewCurrentImageByBill
                String baoxiaoKey = Constants.BAOXIAO_KEY + "." + billId;
                Task task = workFlowService.findTaskByBussinessKey(baoxiaoKey);
                map = workFlowService.findProcessDefinitionByTaskId(task.getId());
            }
            ProcessDefinition pd = (ProcessDefinition) map.get("processDefinition");
            ActivityImpl activity = (ActivityImpl) map.get("activity");
            modelAndView.addObject("acs", activity);
            modelAndView.addObject("deploymentId", pd.getDeploymentId());
            modelAndView.addObject("imageName", pd.getDiagramResourceName());
        } catch (Exception e) {
            modelAndView.addObject("massage","该流程已经不存在");
        }
        return modelAndView;
    }

    @RequestMapping(value = "/viewTaskForm")
    public ModelAndView viewTaskForm(String taskId) {
        ModelAndView modelAndView = new ModelAndView();
        BaoxiaoBill baoxiaoBill = workFlowService.findBaoxiaoBillByTaskId(taskId);
        List<Comment> comment = workFlowService.findCommentByTaskId(taskId);
        List<String> outCome = workFlowService.findOutComeListByTaskId(taskId);
        modelAndView.addObject("baoxiaoBill", baoxiaoBill);
        modelAndView.addObject("outcomeList", outCome);
        modelAndView.addObject("commentList", comment);
        modelAndView.addObject("taskId", taskId);
        modelAndView.setViewName("approve_baoxiao");
        return modelAndView;
    }

    @RequestMapping(value = "/submitTask")
    public String submitTask(long id, String taskId, String comment, String outcome) {
        ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
        workFlowService.saveSubmitTask(id, taskId, comment, outcome, activeUser.getUsername());
        return "redirect:/myTaskList";
    }

    @RequestMapping(value = "/viewHisComment")
    public ModelAndView viewHisComment(long id) {
        ModelAndView modelAndView = new ModelAndView();
        BaoxiaoBill baoxiao = baoxiaoService.findBaoxiaoBillById(id);
        List<Comment> comment = workFlowService.findCommentByBaoxiaoBillId(id);
        modelAndView.addObject("baoxiaoBill", baoxiao);
        modelAndView.addObject("commentList", comment);
        modelAndView.setViewName("workflow_commentlist");
        return modelAndView;
    }

    @RequestMapping(value = "/leaveBillAction_delete")
    public void leaveBillAction_delete(long id){
        baoxiaoService.deleteBaoxiaoBillById(id);
    }

}
