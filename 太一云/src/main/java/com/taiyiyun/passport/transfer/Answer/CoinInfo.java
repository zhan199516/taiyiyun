package com.taiyiyun.passport.transfer.Answer;

import java.math.BigDecimal;

public class CoinInfo {
    //显示
    private String display;
    //名称
    private String name;
    //图标
    private String logo;
    //每个币价值人民币
    private BigDecimal coinprice;
    //此币种允许转账的最小值
    private BigDecimal quota;

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getName() {
        if(name == null){
            return null;
        } else {
            return name.toUpperCase();
        }
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

    public BigDecimal getCoinprice() {
        return coinprice;
    }

    public void setCoinprice(BigDecimal coinprice) {
        this.coinprice = coinprice;
    }

    public BigDecimal getQuota() {
        return quota;
    }

    public void setQuota(BigDecimal quota) {
        this.quota = quota;
    }
}
