package com.taiyiyun.passport.po.group;

import com.taiyiyun.passport.po.PublicUser;

import javax.persistence.Transient;
import java.util.Date;

/**
 * Created by nina on 2017/11/14.
 */
public class GroupMember {
    /*群成员id*/
    private Long id;
    /*群id*/
    private String groupId;
    /*群id数组**/
    private String [] groupIds;
    /*用户id*/
    private String userId;
    /*用户id数组**/
    private String [] userIds;
    /*加入时间*/
    private Date joinTime;
    /*入群状态（待审批：0； 已入群：1）*/
    private Integer joinState;//建群的时候默认为1
    /*入群类型：群主邀请：0；   群成员邀请：1；    扫码加入：2；  搜索加入：3；*/
    private Integer joinType;
    /*邀请人（用户ID） 邀请人：入群类型为“群主邀请”“群成员邀请”时，该字段值有意义*/
    private String inviter;
    /*邀请理由：入群类型为“群成员邀请”时，该字段值有意义*/
    private String invitedReason;
    /*群内昵称，默认username*/
    private String userName;
    /*群内昵称，默认username*/
    private String nikeName;
    /*显示昵称 1-是；0-否*/
    private Integer showNikeName;
    /*消息免打扰 1-是；0-否*/
    private Integer msgReceiveType;
    /*聊天置顶 1-是；0-否*/
    private Integer topTalk;
    /*群成员头像地址*/
    private String avatarUrl;
    /*显示群昵称是否为默认值：1-是；0-否*/
    private Integer showNikeNameDefault;
    /*消息免打扰是否为默认值：1-是；0-否*/
    private Integer msgReceiveTypeDefault;
    /*是否置顶是否为默认值：1-是；0-否*/
    private Integer topTalkDefault;
    /*昵称被设置：1-是；0-否*/
    private Integer nikeNameAltered;
    //判断修改群成员是否需要发送消息，只有设置群成员昵称的修改才需要发送消息
    @Transient
    private Boolean needSendMsg = false;
    /*群组中成员数量*/
    private Long memberCount;
    /**查询记录数*/
    private Integer offset;
    /**群信息**/
    private Group group;
    /**用户信息**/
    private PublicUser user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(Date joinTime) {
        this.joinTime = joinTime;
    }

    public Integer getJoinState() {
        return joinState;
    }

    public void setJoinState(Integer joinState) {
        this.joinState = joinState;
    }

    public int getJoinType() {
        return joinType;
    }

    public void setJoinType(Integer joinType) {
        this.joinType = joinType;
    }

    public String getInviter() {
        return inviter;
    }

    public void setInviter(String inviter) {
        this.inviter = inviter;
    }

    public String getInvitedReason() {
        return invitedReason;
    }

    public void setInvitedReason(String invitedReason) {
        this.invitedReason = invitedReason;
    }

    public String getNikeName() {
        return nikeName;
    }

    public void setNikeName(String nikeName) {
        this.nikeName = nikeName;
    }

    public Integer getShowNikeName() {
        return showNikeName;
    }

    public void setShowNikeName(int showNikeName) {
        this.showNikeName = showNikeName;
    }

    public Integer getMsgReceiveType() {
        return msgReceiveType;
    }

    public void setMsgReceiveType(int msgReceiveType) {
        this.msgReceiveType = msgReceiveType;
    }
    public Integer getTopTalk() {
        return topTalk;
    }
    public void setTopTalk(Integer topTalk) {
        this.topTalk = topTalk;
    }
    public String getAvatarUrl() {
        return avatarUrl;
    }
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Boolean getNeedSendMsg() {
        return needSendMsg;
    }

    public void setNeedSendMsg(Boolean needSendMsg) {
        this.needSendMsg = needSendMsg;
    }

    public Integer getShowNikeNameDefault() {
        return showNikeNameDefault;
    }

    public void setShowNikeNameDefault(Integer showNikeNameDefault) {
        this.showNikeNameDefault = showNikeNameDefault;
    }

    public Integer getMsgReceiveTypeDefault() {
        return msgReceiveTypeDefault;
    }

    public void setMsgReceiveTypeDefault(Integer msgReceiveTypeDefault) {
        this.msgReceiveTypeDefault = msgReceiveTypeDefault;
    }

    public Integer getTopTalkDefault() {
        return topTalkDefault;
    }

    public void setTopTalkDefault(Integer topTalkDefault) {
        this.topTalkDefault = topTalkDefault;
    }

    public Integer getNikeNameAltered() {
        return nikeNameAltered;
    }

    public void setNikeNameAltered(Integer nikeNameAltered) {
        this.nikeNameAltered = nikeNameAltered;
    }

    public Long getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Long memberCount) {
        this.memberCount = memberCount;
    }

    public void setShowNikeName(Integer showNikeName) {
        this.showNikeName = showNikeName;
    }

    public void setMsgReceiveType(Integer msgReceiveType) {
        this.msgReceiveType = msgReceiveType;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String[] getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(String[] groupIds) {
        this.groupIds = groupIds;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public PublicUser getUser() {
        return user;
    }

    public void setUser(PublicUser user) {
        this.user = user;
    }

    public String[] getUserIds() {
        return userIds;
    }

    public void setUserIds(String[] userIds) {
        this.userIds = userIds;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


}
