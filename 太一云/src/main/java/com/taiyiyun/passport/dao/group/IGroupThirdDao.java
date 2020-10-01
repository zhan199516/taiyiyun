package com.taiyiyun.passport.dao.group;

import com.taiyiyun.passport.po.group.GroupThird;

import java.util.List;

/**
 * Created by nina on 2018/1/12.
 */
public interface IGroupThirdDao {

    GroupThird selectByPrimarykey(String gtId);

    GroupThird selectGroupThirdByThirdKey(String thirdKey);

    List<String> selectAllThirdKey();

}
