package com.taiyiyun.passport.po.asset;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Created by okdos on 2017/7/7.
 * 转账返回
 */
public class TransferAnswer {

    public String getTradeNo(){
        if(this.tradeId == null){
            return null;
        } else {
            return String.format("%08d", this.tradeId);
        }
    }

    //交易id，用于未来交易查询，所有交易不会重复
    @JsonIgnore
    @JSONField(serialize=false)
    private Long tradeId;
    //平台名称
    private String platformName;
    //货币名称
    private String coinName;
    //交易提交时间
    private long createTime;


    //平台id
    private String platformId;
    //币种id
    private String coinId;
    //转账数量
    @JsonProperty("amount")
    @JSONField(name="amount")
    private BigDecimal amount;
    //手续费
    @JsonProperty("fee")
    @JSONField(name="fee")
    private BigDecimal fee;
    //转账发起人
    private String fromUserId;
    //转账发起账号
    @JsonIgnore
    @JSONField(serialize=false)
    private String fromUuid;
    //转账对象
    private String toUserId;
    //转账发起账号
    @JsonIgnore
    @JSONField(serialize=false)
    private String toUuid;
    //转账附加消息
    private String text;
    //转账过期时间，可以不填写，默认为0
    private long expireTime;
    //冻结id
    private Long frozenId;
    //交易状态
    private int status;
    //交易时候的价格
    private BigDecimal worthRmbApply;

    //防止重复转账的token
    private String repeatToken;


//    public double getAmount() {
//        return NumberUtil.fromDecimal(this.amount);
//    }
//
//    public void setAmount(double amount) {
//        this.amount = NumberUtil.fromDouble(amount);
//    }
//
//    public double getFee() {
//        return NumberUtil.fromDecimal(this.fee);
//    }
//
//    public void setFee(double fee) {
//        this.fee = NumberUtil.fromDouble(fee);
//    }


    public BigDecimal getWorthRmbApply() {
        return worthRmbApply;
    }

    public void setWorthRmbApply(BigDecimal worthRmbApply) {
        this.worthRmbApply = worthRmbApply;
    }

    public String getFromUuid() {
        return fromUuid;
    }

    public void setFromUuid(String fromUuid) {
        this.fromUuid = fromUuid;
    }

    public String getToUuid() {
        return toUuid;
    }

    public void setToUuid(String toUuid) {
        this.toUuid = toUuid;
    }

    public Long getFrozenId() {
        return frozenId;
    }

    public void setFrozenId(Long frozenId) {
        this.frozenId = frozenId;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public String getCoinId() {
        return coinId;
    }

    public void setCoinId(String coinId) {
        this.coinId = coinId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public String getCoinName() {
        return coinName;
    }

    public void setCoinName(String coinName) {
        this.coinName = coinName;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public Long getTradeId() {
        return tradeId;
    }

    public void setTradeId(Long tradeId) {
        this.tradeId = tradeId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public String getRepeatToken() {
        return repeatToken;
    }

    public void setRepeatToken(String repeatToken) {
        this.repeatToken = repeatToken;
    }
}
