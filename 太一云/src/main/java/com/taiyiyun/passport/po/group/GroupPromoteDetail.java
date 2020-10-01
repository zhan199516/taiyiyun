package com.taiyiyun.passport.po.group;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by zhangjun on 2018/1/23.
 */

public class GroupPromoteDetail implements Serializable {

    /* 主键Id*/
    private Long id;
    /*交易id*/
    private Long tradeId;
    /*冻结交易id*/
    private Long frozenId;
    /*群组Id*/
    private String groupId;
    /*群归属用户Id*/
    private String ownerId;
    /*币种Id*/
    private String coinId;
    /*推广目标用户*/
    private String targetUserId;
    /*推广花费成本*/
    private BigDecimal amount;
    /*手续费*/
    private BigDecimal charge;
    /*转账状态*/
    private Integer transferStatus;
    /*转账时间*/
    private Date transferTime;
    /*过期时间*/
    private Long expireTime;
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

    public Long getTradeId() {
        return tradeId;
    }

    public void setTradeId(Long tradeId) {
        this.tradeId = tradeId;
    }

    public Long getFrozenId() {
        return frozenId;
    }

    public void setFrozenId(Long frozenId) {
        this.frozenId = frozenId;
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

    public String getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getCharge() {
        return charge;
    }

    public void setCharge(BigDecimal charge) {
        this.charge = charge;
    }

    public Integer getTransferStatus() {
        return transferStatus;
    }

    public void setTransferStatus(Integer transferStatus) {
        this.transferStatus = transferStatus;
    }

    public Date getTransferTime() {
        return transferTime;
    }

    public void setTransferTime(Date transferTime) {
        this.transferTime = transferTime;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
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
}
