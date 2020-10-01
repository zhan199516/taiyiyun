package com.taiyiyun.passport.po.group;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nina on 2017/11/14.
 */
public class AddGroupUserParam {
    /*群id*/
    private String groupId;
    /*被添加的成员用户id列表*/
    private List<String> userIdList = new ArrayList<>();
    /*进群类型 0：群主邀请，1：群成员邀请，2：扫码进入，3：群搜索*/
    private int joinType;
    /*邀请人用户id（非邀请入群时为空）*/
    private String inviterId;
    /*邀请原因（非邀请入群时为空）*/
    private String inviteReason;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public List<String> getUserIdList() {
        return userIdList;
    }

    public void setUserIdList(List<String> userIdList) {
        this.userIdList = userIdList;
    }

    public int getJoinType() {
        return joinType;
    }

    public void setJoinType(int joinType) {
        this.joinType = joinType;
    }

    public String getInviterId() {
        return inviterId;
    }

    public void setInviterId(String inviterId) {
        this.inviterId = inviterId;
    }

    public String getInviteReason() {
        return inviteReason;
    }

    public void setInviteReason(String inviteReason) {
        this.inviteReason = inviteReason;
    }
}