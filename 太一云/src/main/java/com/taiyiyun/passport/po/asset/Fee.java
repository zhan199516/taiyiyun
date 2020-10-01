package com.taiyiyun.passport.po.asset;

import java.math.BigDecimal;

/**
 * Created by okdos on 2017/7/7.
 * 计算的手续费
 */
public class Fee {
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCoinId() {
        return coinId;
    }

    public void setCoinId(String coinId) {
        this.coinId = coinId;
    }

    public String getCoinName() {
        return coinName;
    }

    public void setCoinName(String coinName) {
        this.coinName = coinName;
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

    public BigDecimal getSaving() {
        return saving;
    }

    public void setSaving(BigDecimal saving) {
        this.saving = saving;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    //用户id
    private String userId;
    //平台id
    private String platformId;
    //币种id
    private String coinId;
    //币种名称
    private String coinName;
    //准备转账数量
    private BigDecimal amount;
    //手续费
    private BigDecimal fee;
    //可用余额
    private BigDecimal saving;
}
