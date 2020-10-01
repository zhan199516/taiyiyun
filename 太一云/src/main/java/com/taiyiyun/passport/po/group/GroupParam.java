package com.taiyiyun.passport.po.group;

import java.util.List;

/**
 * Created by nina on 2017/11/14.
 */
public class GroupParam {
    private String ownerId;
    private List<String> userIdList;

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public List<String> getUserIdList() {
        return userIdList;
    }

    public void setUserIdList(List<String> userIdList) {
        this.userIdList = userIdList;
    }
}
