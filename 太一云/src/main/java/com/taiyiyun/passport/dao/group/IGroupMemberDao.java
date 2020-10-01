package com.taiyiyun.passport.dao.group;

import com.taiyiyun.passport.po.group.GroupMember;

import java.util.List;
import java.util.Map;

/**
 * Created by nina on 2017/11/14.
 */
public interface IGroupMemberDao {
    void insertSelective(GroupMember groupMember);

    GroupMember selectGroupMemberByGroupIdAndUserId(Map<String, String> params);

    GroupMember selectTheEarliestMem(String groupId);

    void authGroupMemberByGroupIdAndUserId(Map<String, String> params);

    void deleteGroupMemberCanNotOwner(Map<String, String> params);

    void deleteGroupMemberCanBeAll(Map<String, String> params);

    int selectGroupMemberCounts(String groupId);

    void updateByPrimaryKeySelective(GroupMember groupMember);

	List<String> selectGroupMemberUserIdByGroupId(String groupId);

	GroupMember selectGroupUserInfoOnLogin(Map<String, String> params);

	List<String> selectGroupIds(String userId);

	List<GroupMember> selectGroupMemsByGroupId(String groupId);

    List<GroupMember> listGroupMemberCount(String userId);

    List<GroupMember> getMemberCountByGroupIds(GroupMember groupMember);

    List<GroupMember> listMembersByUserId(GroupMember groupMember);

    List<GroupMember> listMembersByGroupIds(GroupMember groupMember);

    List<GroupMember> listMembersByContent(GroupMember groupMember);

    List<GroupMember> listMembersByIds(GroupMember groupMember);

    int updateTopById(GroupMember groupMember);

    int updateDisturbById(GroupMember groupMember);

    List<String> selectSetDisturbUserIds(String groupId);
}
