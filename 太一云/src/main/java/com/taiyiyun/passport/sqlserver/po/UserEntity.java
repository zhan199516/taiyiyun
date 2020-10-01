package com.taiyiyun.passport.sqlserver.po;

import java.util.Date;

/**
 * Created by nina on 2017/10/26.
 */
public class UserEntity {
    /*用户实体ID*/
    private String userEntityId;
    /*实体ID*/
    private String entityId;
    /*用户ID*/
    private String uuid;
    /*用户实体状态0-禁用，1-启用 默认：0*/
    private int status = 0;
    /*默认交易金额（默认10000）*/
    private int defaultAmount = 10000;
    /*审核失败的信息(默认空字符串)*/
    private String failMessage = "";
    /*操作账号（管理员。。。。）（默认0）*/
    private long operationUserId = 0L;
    /*操作员登录名（默认空字符串）*/
    private String operUserName = "";
    /*操作时间*/
    private Date operTime;
    /*审核渠道：0-云签审核通过，1：人工审核，2：公安一所审核通过*/
    private int auditChannel = 0;

    public String getUserEntityId() {
        return userEntityId;
    }

    public void setUserEntityId(String userEntityId) {
        this.userEntityId = userEntityId;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getDefaultAmount() {
        return defaultAmount;
    }

    public void setDefaultAmount(int defaultAmount) {
        this.defaultAmount = defaultAmount;
    }

    public String getFailMessage() {
        return failMessage;
    }

    public void setFailMessage(String failMessage) {
        this.failMessage = failMessage;
    }

    public long getOperationUserId() {
        return operationUserId;
    }

    public void setOperationUserId(long operationUserId) {
        this.operationUserId = operationUserId;
    }

    public String getOperUserName() {
        return operUserName;
    }

    public void setOperUserName(String operUserName) {
        this.operUserName = operUserName;
    }

    public Date getOperTime() {
        return operTime;
    }

    public void setOperTime(Date operTime) {
        this.operTime = operTime;
    }

    public int getAuditChannel() {
        return auditChannel;
    }

    public void setAuditChannel(int auditChannel) {
        this.auditChannel = auditChannel;
    }
}
