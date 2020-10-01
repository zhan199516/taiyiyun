package com.taiyiyun.passport.po.group;

/**
 * Created by nina on 2018/1/12.
 */
public class GroupThirdGroup {
    /*扫码第三方id*/
    private String gtId;
    /*第三方群id*/
    private String groupId;
    /*入群顺序*/
    private Integer sort;

    public String getGtId() {
        return gtId;
    }

    public void setGtId(String gtId) {
        this.gtId = gtId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }
}
