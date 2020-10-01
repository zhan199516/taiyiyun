package com.taiyiyun.passport.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import com.taiyiyun.passport.dao.ICollectExpressionDao;
import com.taiyiyun.passport.po.chat.CollectExpression;
import com.taiyiyun.passport.service.CollectExpressionService;

/**
 * Created by elan on 2017/11/22.
 */
@Service
public class CollectExpressionServiceImpl implements CollectExpressionService {

    @Resource
    private ICollectExpressionDao dao;


    @Override
    public void save(CollectExpression collectExpression) {

         dao.save(collectExpression);

    }

	@Override
	public List<CollectExpression> getByUserId(String userId) {
		return dao.getByUserId(userId);
	}

	
	@Override
	public CollectExpression getById(Integer id) {
		return dao.getById(id);
	}

	@Override
	public void delExpression(List<Integer> idList,String userId) {
		dao.delExpression(idList,userId);
	}
}
