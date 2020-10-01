package com.taiyiyun.passport.transfer.Answer;

import java.math.BigDecimal;

/**
 * Created by okdos on 2017/7/17.
 * 某个币种的资产
 */
public class CoinOwn {
    public String getCoinName() {
        return coinName;
    }

    public void setCoinName(String coinName) {
        this.coinName = coinName;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }

    public BigDecimal getBlockedFunds() {
        return blockedFunds;
    }

    public void setBlockedFunds(BigDecimal blockedFunds) {
        this.blockedFunds = blockedFunds;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getChinaName() {
        return chinaName;
    }

    public void setChinaName(String chinaName) {
        this.chinaName = chinaName;
    }

    public BigDecimal getValuation() {
        return valuation;
    }

    public void setValuation(BigDecimal valuation) {
        this.valuation = valuation;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    private String coinName;
    private BigDecimal availableBalance;
    private BigDecimal blockedFunds;
    private String logo;
    private String chinaName;
    private BigDecimal valuation;
    private BigDecimal fee;
}
