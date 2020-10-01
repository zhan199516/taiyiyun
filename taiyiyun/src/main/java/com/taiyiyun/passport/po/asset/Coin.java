package com.taiyiyun.passport.po.asset;

import java.math.BigDecimal;

/**
 * Created by okdos on 2017/7/7.
 * 币种信息
 */
public class Coin {
    public String getCoinId() {
        return coinId;
    }

    public void setCoinId(String coinId) {
        this.coinId = coinId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public BigDecimal getWorthRmb() {
        return worthRmb;
    }

    public void setWorthRmb(BigDecimal worthRmb) {
        this.worthRmb = worthRmb;
    }

    public BigDecimal getAsset() {
        return asset;
    }

    public void setAsset(BigDecimal asset) {
        this.asset = asset;
    }

    public BigDecimal getFrozen() {
        return frozen;
    }

    public void setFrozen(BigDecimal frozen) {
        this.frozen = frozen;
    }

    public BigDecimal getFeeBalance() {
        return feeBalance;
    }

    public void setFeeBalance(BigDecimal feeBalance) {
        this.feeBalance = feeBalance;
    }

    public BigDecimal getFeeMin() {
        return feeMin;
    }

    public void setFeeMin(BigDecimal feeMin) {
        this.feeMin = feeMin;
    }

    //币种id
    private String coinId;
    //币种名称
    private String name;
    //币种logo
    private String logo;
    //每1.0单位虚拟币价值人民币
    private BigDecimal worthRmb;
    //资产数量
    private BigDecimal asset;
    //资产冻结数量
    private BigDecimal frozen;
    //手续费比例
    private BigDecimal feeBalance;
    //最低手续费
    private BigDecimal feeMin;

}
