package com.web.oa.service.impl;

import com.web.oa.mapper.BaoxiaoBillMapper;
import com.web.oa.pojo.BaoxiaoBill;
import com.web.oa.pojo.BaoxiaoBillExample;
import com.web.oa.service.BaoxiaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BaoxiaoServiceImpl implements BaoxiaoService {

    @Autowired
    private BaoxiaoBillMapper baoxiaoBillMapper;

    @Override
    public List<BaoxiaoBill> findBaoxiaoBillListByUser(Long userid) {
        BaoxiaoBillExample baoxiaoBillExample=new BaoxiaoBillExample();
        BaoxiaoBillExample.Criteria criteria = baoxiaoBillExample.createCriteria();
        criteria.andUserIdEqualTo(userid);
        List<BaoxiaoBill> baoxiaoBills = baoxiaoBillMapper.selectByExample(baoxiaoBillExample);
        if(null!=baoxiaoBills&&baoxiaoBills.size()>0){
            return baoxiaoBills;
        }
        return null;
    }

    @Override
    public void saveBaoxiao(BaoxiaoBill baoxiaoBill) {
        Long id = baoxiaoBill.getId();
        if(null==id){
            baoxiaoBillMapper.insert(baoxiaoBill);
        }else {
            baoxiaoBillMapper.updateByPrimaryKey(baoxiaoBill);
        }
    }

    @Override
    public BaoxiaoBill findBaoxiaoBillById(Long id) {
        return baoxiaoBillMapper.selectByPrimaryKey(id);
    }

    @Override
    public void deleteBaoxiaoBillById(Long id) {
        baoxiaoBillMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<BaoxiaoBill> findLeaveBillListByUser(Long id) {
        BaoxiaoBillExample example = new BaoxiaoBillExample();
        BaoxiaoBillExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(id);
        return baoxiaoBillMapper.selectByExample(example);
    }
}
