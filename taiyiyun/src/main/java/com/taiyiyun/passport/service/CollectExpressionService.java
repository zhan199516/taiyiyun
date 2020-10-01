package com.taiyiyun.passport.service;

import com.taiyiyun.passport.po.chat.CollectExpression;

import java.util.List;

/**
 * Created by elan on 2017/11/22.
 */
public interface CollectExpressionService {

	public List<CollectExpression> getByUserId(String userId);

	public void save(CollectExpression collectExpression);
	
	public CollectExpression getById(Integer expId);

	public void delExpression(List<Integer> idList,String userId);

}
