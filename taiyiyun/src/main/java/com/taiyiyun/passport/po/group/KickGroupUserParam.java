package com.taiyiyun.passport.po.group;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nina on 2017/11/15.
 */
public class KickGroupUserParam {
    /*群id*/
    private String groupId;
    /*被添加的成员用户id列表*/
    private List<String> userIdList = new ArrayList<>();

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
}
