package com.taiyiyun.passport.dao.group;

import com.taiyiyun.passport.po.group.GroupThirdGroup;

import java.util.List;

/**
 * Created by nina on 2018/1/12.
 */
public interface IGroupThirdGroupDao {

    /*根据扫码第三id查询与其关联的所有群id*/
    List<GroupThirdGroup> selectGroupThirdGroupsByGtId(String gtId);
}
