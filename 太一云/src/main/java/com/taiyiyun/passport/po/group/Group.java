package com.taiyiyun.passport.po.group;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by nina on 2017/11/8.
 */
public class Group implements Serializable{
    /*群ID*/
    private String groupId;
    /*群id数组*/
    private String [] groupIds;
    /*群名称*/
    private String groupName;//默认为“群聊”
    /*群名片*/
    private String description;
    /*群头像*/
    private String groupHeader;
    /*群主ID*/
    private String ownerId;
    /*邀请类型*/
    private int inviteType;//1-允许群成员邀请；0-不允许群成员邀请
    /*成员设置群名称权限*/
    private int modifyRight;//1-是，0-否 默认为
    /*群类型 0-公有群；1-私有群*/
    private int groupType;
    /*群状态：是否删除*/
    private int groupState;//1-是，0-否
    /*创建群时间*/
    private Date createTime;
    /*是否需要审核 1-是，0-否*/
    private int needAuth;
    /*是否为推广群组 0：否  1：是*/
    private Integer isPromote;
    /*邀请类型是否为默认值：1-是，0-否*/
    private int inviteTypeDefault;
    /*成员设置群名称权限是否为默认值：1-是，0-否*/
    private int modifyRightDefault;
    /*群类型是否为默认值：1-是，0-否*/
    private int groupTypeDefault;
    /*是否需要群主审批默认值：1-是，0-否*/
    private int needAuthDefault;
    /*群信息变更时间*/
    private long updateTime;
    /*群成员ID集合*/
    private List<String> userIdList = new ArrayList<>();
    /*群成员集合*/
    private List<GroupMember> groupMembers = new ArrayList<>();
    /*群成员个数*/
    private Integer memberAmount;


    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGroupHeader() {
        return groupHeader;
    }

    public void setGroupHeader(String groupHeader) {
        this.groupHeader = groupHeader;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public int getInviteType() {
        return inviteType;
    }

    public void setInviteType(int inviteType) {
        this.inviteType = inviteType;
    }

    public int getModifyRight() {
        return modifyRight;
    }

    public void setModifyRight(int modifyRight) {
        this.modifyRight = modifyRight;
    }

    public int getGroupType() {
        return groupType;
    }

    public void setGroupType(int groupType) {
        this.groupType = groupType;
    }

    public int getGroupState() {
        return groupState;
    }

    public void setGroupState(int groupState) {
        this.groupState = groupState;
    }

    public Date getCreateTime() {
        return createTime;
    }
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getNeedAuth() {
        return needAuth;
    }
    public void setNeedAuth(int needAuth) {
        this.needAuth = needAuth;
    }

    public Integer getIsPromote() {
        return isPromote;
    }

    public void setIsPromote(Integer isPromote) {
        this.isPromote = isPromote;
    }

    public List<String> getUserIdList() {
        return userIdList;
    }
    public void setUserIdList(List<String> userIdList) {
        this.userIdList = userIdList;
    }

    public List<GroupMember> getGroupMembers() {
        return groupMembers;
    }
    public void setGroupMembers(List<GroupMember> groupMembers) {
        this.groupMembers = groupMembers;
    }

    public int getInviteTypeDefault() {
        return inviteTypeDefault;
    }

    public void setInviteTypeDefault(int inviteTypeDefault) {
        this.inviteTypeDefault = inviteTypeDefault;
    }

    public int getModifyRightDefault() {
        return modifyRightDefault;
    }

    public void setModifyRightDefault(int modifyRightDefault) {
        this.modifyRightDefault = modifyRightDefault;
    }

    public int getGroupTypeDefault() {
        return groupTypeDefault;
    }

    public void setGroupTypeDefault(int groupTypeDefault) {
        this.groupTypeDefault = groupTypeDefault;
    }

    public int getNeedAuthDefault() {
        return needAuthDefault;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public void setNeedAuthDefault(int needAuthDefault) {
        this.needAuthDefault = needAuthDefault;
    }

    public Integer getMemberAmount() {
        return memberAmount;
    }

    public void setMemberAmount(Integer memberAmount) {
        this.memberAmount = memberAmount;
    }

    public String[] getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(String[] groupIds) {
        this.groupIds = groupIds;
    }
}