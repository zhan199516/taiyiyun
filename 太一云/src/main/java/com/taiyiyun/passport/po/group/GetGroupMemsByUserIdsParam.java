package com.taiyiyun.passport.po.group;

import java.util.List;

/**
 * Created by nina on 2017/12/8.
 */
public class GetGroupMemsByUserIdsParam {
    private String groupId;
    private List<String> userIds;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }
}
