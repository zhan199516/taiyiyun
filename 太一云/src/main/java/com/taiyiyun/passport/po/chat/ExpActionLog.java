package com.taiyiyun.passport.po.chat;

import java.io.Serializable;

/**
 * Created by elan on 2017/11/22.
 */
public class ExpActionLog implements Serializable{

    /**
     *
     */
    private static final long serialVersionUID = -2392404637393261927L;

    private int id;

    private String userId;

    private int collectExpId;

    private int action;   //操作  1、添加表情；2、删除表情；3、上传图片；

    private long createTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getCollectExpId() {
        return collectExpId;
    }

    public void setCollectExpId(int collectExpId) {
        this.collectExpId = collectExpId;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}
