package com.taiyiyun.passport.dao;

import com.taiyiyun.passport.po.chat.CollectExpression;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by elan on 2017/11/22.
 */
public interface ICollectExpressionDao {

    public List<CollectExpression> getByUserId(@Param("userId")String userId);

    public void save(CollectExpression collectExpression);
    
    public CollectExpression getById(@Param("id")Integer Id);

    public void delExpression(@Param("idList")List<Integer> idList, @Param("userId")String userId);
}
