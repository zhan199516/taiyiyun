package com.taiyiyun.passport.po.group;

import java.util.Date;

/**
 * 扫码入群第三方实体
 * Created by nina on 2018/1/12.
 */
public class GroupThird {
    private String gtId;
    /*第三方名称*/
    private String thirdName;
    /*第三方码中关键字*/
    private String thirdKey;
    /*创建时间*/
    private Date createTime;
    /*状态：0-禁用；1-启用*/
    private Integer status;


    public String getGtId() {
        return gtId;
    }

    public void setGtId(String gtId) {
        this.gtId = gtId;
    }

    public String getThirdName() {
        return thirdName;
    }

    public void setThirdName(String thirdName) {
        this.thirdName = thirdName;
    }

    public String getThirdKey() {
        return thirdKey;
    }

    public void setThirdKey(String thirdKey) {
        this.thirdKey = thirdKey;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}