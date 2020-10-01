package com.taiyiyun.passport.po.message;

import java.io.Serializable;
import java.util.List;

/**
 * Created by okdos on 2017/6/27.
 */
public class UserListEntity implements Serializable {

    public List<String> getUserIdList() {
        return userIdList;
    }

    public void setUserIdList(List<String> userIdList) {
        this.userIdList = userIdList;
    }

    List<String> userIdList;


}
