package com.taiyiyun.passport.dao.group;

import com.taiyiyun.passport.po.group.Group;

import java.util.List;
import java.util.Map;

/**
 * Created by nina on 2017/11/8.
 */
public interface IGroupDao {
    List<Group> selectAll();
    List<Group> selectBySearchKey(Map<String, String> param);
    void insertSelective(Group group);
    Group selectByPrimarykey(String groupId);
    Group selectByIdAndPromote(String groupId);
    void deleteByPrimaryKey(String groupId);
    void updateGroupOwner(Map<String, String> params);
    void updateByPrimaryKeySelective(Group group);
    List<Group> listGroupByIds(Group group);
}
