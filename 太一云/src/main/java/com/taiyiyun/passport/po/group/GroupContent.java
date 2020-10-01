package com.taiyiyun.passport.po.group;

import java.io.Serializable;
import java.util.List;

/**
 * Created by nina on 2017/12/13.
 */
public class GroupContent implements Serializable{
    private static final long serialVersionUID = 1L;

    private String groupId = "";
    private String groupName = "";
    private String description = "";
    private String groupHeader = "";
    private String ownerId = "";
    private Integer inviteType = -1;
    private Integer modifyRight = -1;
    private Integer groupType = -1;
    private Integer index;
    private Integer needAuth = -1;
    private List<String> memIds;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

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

    public Integer getInviteType() {
        return inviteType;
    }

    public void setInviteType(Integer inviteType) {
        this.inviteType = inviteType;
    }

    public Integer getModifyRight() {
        return modifyRight;
    }

    public void setModifyRight(Integer modifyRight) {
        this.modifyRight = modifyRight;
    }

    public Integer getGroupType() {
        return groupType;
    }

    public void setGroupType(Integer groupType) {
        this.groupType = groupType;
    }

    public List<String> getMemIds() {
        return memIds;
    }

    public void setMemIds(List<String> memIds) {
        this.memIds = memIds;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Integer getNeedAuth() {
        return needAuth;
    }

    public void setNeedAuth(Integer needAuth) {
        this.needAuth = needAuth;
    }
}
