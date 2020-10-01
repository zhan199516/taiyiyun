package com.taiyiyun.passport.sqlserver.po;

import java.util.Date;

/**
 * Created by nina on 2017/10/26.
 */
public class Picture {
    private String pid;
    /*图片类型ID*/
    private int pTypeId;
    /*图片名称*/
    private String fileName;
    /*实体ID*/
    private String entityId;
    /*是否对外公开，0：不公开，1：公开 (默认0)*/
    private int isPublic = 0;
    /*创建时间*/
    private Date creationTime;

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public int getpTypeId() {
        return pTypeId;
    }

    public void setpTypeId(int pTypeId) {
        this.pTypeId = pTypeId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public int getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(int isPublic) {
        this.isPublic = isPublic;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }
}
