package com.taiyiyun.passport.sqlserver.po;

import java.util.Date;

/**
 * Created by nina on 2017/10/25.
 */
public class Entity {
    /*实体ID*/
    private String entityId;
    /*实体唯一标识,个人:身份证或护照号;机构:营业执照编号,空身份：空值*/
    private String entityUnqueId;
    /*实体类型，0：空身份；1：个人；2：机构;*/
    private int typeId;
    /*信息认证状态 0：未审核，1：审核通过，2：审核失败*/
    private int status;
    /*实体名字，空身份：空字符串，个人：英语名字，机构：机构英文名*/
    private String entityName;
    /*等级ID，0：普通，1：可信机构,2:正在申请成为可信机构的企业*/
    private int gradeId;
    /*创建时间*/
    private Date creationTime;

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getEntityUnqueId() {
        return entityUnqueId;
    }

    public void setEntityUnqueId(String entityUnqueId) {
        this.entityUnqueId = entityUnqueId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public int getGradeId() {
        return gradeId;
    }

    public void setGradeId(int gradeId) {
        this.gradeId = gradeId;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }
}
