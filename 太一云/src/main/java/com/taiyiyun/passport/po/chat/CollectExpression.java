package com.taiyiyun.passport.po.chat;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by elan on 2017/11/22.
 */
public class CollectExpression implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -6292404637393261927L;

    private Integer id;

    private String userId;

    private String picMd5;

    private Date createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPicMd5() {
        return picMd5;
    }

    public void setPicMd5(String picMd5) {
        this.picMd5 = picMd5;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
