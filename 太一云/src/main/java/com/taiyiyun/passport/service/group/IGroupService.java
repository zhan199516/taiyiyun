package com.taiyiyun.passport.service.group;

import com.alibaba.fastjson.JSONObject;
import com.taiyiyun.passport.bean.UserDetails;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.mqtt.Message;
import com.taiyiyun.passport.po.BaseResult;
import com.taiyiyun.passport.po.group.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by nina on 2017/11/14.
 */
public interface IGroupService {

	List<Message<JSONObject>> addGroup(PackBundle packBundle,String ownerId,String groupName,Integer isPromote, List<String> userIdList, UserDetails userDetails);

	List<Message<JSONObject>> addGroupUser(PackBundle packBundle, AddGroupUserParam addGroupUserParam, UserDetails userDetails);

    void intoGroupPrize(PackBundle packBundle,Group group,List<String> userIdList,UserDetails userDetails);

    List<Message<JSONObject>> addGroupUserByScanCode(PackBundle packBundle,String thirdKey, UserDetails userDetails);

    List<String> queryAllGroupThirdKeys();

    List<Message<JSONObject>> approveGroupUser(PackBundle packBundle,ApproveGroupUserParam approveGroupUserParam,UserDetails userDetails);

    Group queryGroupByGroupId(String groupId);

    List<Message<JSONObject>> deleteGroupUser(KickGroupUserParam kickGroupUserParam);

    List<Message<JSONObject>> exitGroup(PackBundle packBundle,ExitGroupParam exitGroupParam);

    List<Group> queryBySearchKey(String searchKey);

    void updateGroup(Group group, String groupHeader) throws IOException;

    void updateGroupMember(GroupMember groupMember);

    GroupMember queryGroupMemberByGroupIdAndUserId(String groupId, String userId);

    void changeGroupOwner(PackBundle packBundle,String groupId, String oldOwnerId, String newOwnerId);

    GroupMember queryGroupUserInfoOnLogin(String groupId, String userId);

    List<String> queryGroupIds(String userId);

    List<Group> queryGroupsByGroupIds(List<String> groupIds, String userId);

    List<Group> queryGroups(String userId);

    List<String> queryGroupMemUserIds(String groupId, String userId);

    List<GroupMember> queryGroupMemsByUserIds(GetGroupMemsByUserIdsParam getGroupMemsByUserIdsParam, String userId);

    List<GroupMember> queryGroupMems(String groupId, String userId);

    List<Group> queryGroupMems(List<String> groupIds, String userId);

    int queryGroupMemberCounts(String groupId);

    /**
     * 查询群成员信息
     * @param groupMemberSearchDto
     * @return
     */
    BaseResult<List<Map<String,Object>>> listSearchGroupMembers(PackBundle bundle,GroupMemberSearchDto groupMemberSearchDto);
}
