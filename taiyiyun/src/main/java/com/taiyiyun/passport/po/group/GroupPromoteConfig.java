package com.taiyiyun.passport.po.group;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by zhangjun on 2018/1/23.
 */

public class GroupPromoteConfig implements Serializable {

    /*主键Id*/
    private Long id;
    /*群组Id*/
    private String groupId;
    /*群组归属人Id*/
    private String ownerId;
    /*币种Id*/
    private String coinId;
    /*平台id*/
    private String platformId;
    /*推广成本*/
    private BigDecimal amount;
    /*是否通知*/
    private Integer isNotice;
    /*币数量阀值*/
    private Integer countThreshold;
    /*通知时间间隔（分钟）*/
    private Integer noticeInterval;
    /*通知时间*/
    private Integer noticeHour;
    /*通知开始时间*/
    private Integer noticeBeginHour;
    /*通知结束时间*/
    private Integer noticeEndHour;
    /*执行通知时间*/
    private Date noticeTime;
    /*联系人手机号*/
    private String noticePhone;
    /*联系人邮箱*/
    private String noticeEmail;
    /*修改时间*/
    private Date modifyTime;
    /*创建时间*/
    private Date createTime;
    /*当前页数*/
    private Integer start;
    /*每页记录数*/
    private Integer offset;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getCoinId() {
        return coinId;
    }

    public void setCoinId(String coinId) {
        this.coinId = coinId;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getIsNotice() {
        return isNotice;
    }

    public void setIsNotice(Integer isNotice) {
        this.isNotice = isNotice;
    }

    public Integer getCountThreshold() {
        return countThreshold;
    }

    public void setCountThreshold(Integer countThreshold) {
        this.countThreshold = countThreshold;
    }

    public Integer getNoticeInterval() {
        return noticeInterval;
    }

    public void setNoticeInterval(Integer noticeInterval) {
        this.noticeInterval = noticeInterval;
    }

    public Date getNoticeTime() {
        return noticeTime;
    }

    public void setNoticeTime(Date noticeTime) {
        this.noticeTime = noticeTime;
    }

    public String getNoticePhone() {
        return noticePhone;
    }

    public void setNoticePhone(String noticePhone) {
        this.noticePhone = noticePhone;
    }

    public String getNoticeEmail() {
        return noticeEmail;
    }

    public void setNoticeEmail(String noticeEmail) {
        this.noticeEmail = noticeEmail;
    }

    public Integer getNoticeBeginHour() {
        return noticeBeginHour;
    }

    public void setNoticeBeginHour(Integer noticeBeginHour) {
        this.noticeBeginHour = noticeBeginHour;
    }

    public Integer getNoticeEndHour() {
        return noticeEndHour;
    }

    public void setNoticeEndHour(Integer noticeEndHour) {
        this.noticeEndHour = noticeEndHour;
    }

    public Integer getNoticeHour() {
        return noticeHour;
    }

    public void setNoticeHour(Integer noticeHour) {
        this.noticeHour = noticeHour;
    }
}
