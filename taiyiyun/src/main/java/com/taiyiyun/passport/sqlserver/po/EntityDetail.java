package com.taiyiyun.passport.sqlserver.po;

/**
 * Created by nina on 2017/10/26.
 */
public class EntityDetail {
    /*id*/
    private long id;
    /*实体ID*/
    private String entityId;
    /*信息类型*/
    private String infoKey;
    /*信息内容*/
    private String infoValue;
    /*信息认证状态0：未通过，1：通过(默认0)*/
    private int status = 0;
    /*是否对外公开，0：不公开，1：公开(默认0)*/
    private int isPublic = 0;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getInfoKey() {
        return infoKey;
    }

    public void setInfoKey(String infoKey) {
        this.infoKey = infoKey;
    }

    public String getInfoValue() {
        return infoValue;
    }

    public void setInfoValue(String infoValue) {
        this.infoValue = infoValue;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(int isPublic) {
        this.isPublic = isPublic;
    }
}
