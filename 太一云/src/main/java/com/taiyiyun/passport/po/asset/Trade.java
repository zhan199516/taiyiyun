package com.taiyiyun.passport.po.asset;

import java.math.BigDecimal;

/**
 * Created by okdos on 2017/7/7.
 */
public class Trade extends TransferAnswer{


    //接受时间private
    private long acceptTime;

    private BigDecimal fromOverBegin;
    private BigDecimal fromOverEnd;
    private BigDecimal toOverBegin;
    private BigDecimal toOverEnd;

    private String fromUserName;
    private String toUserName;
    private String platformName;
    private String platformLogo;
    private String coinName;
    private String coinLogo;
    //错误原因
    private String error;

    private BigDecimal worthRmbAccept;

    public BigDecimal getWorthRmbAccept() {
        return worthRmbAccept;
    }

    public void setWorthRmbAccept(BigDecimal worthRmbAccept) {
        this.worthRmbAccept = worthRmbAccept;
    }

    public BigDecimal getFromOverBegin() {
        return fromOverBegin;
    }

    public void setFromOverBegin(BigDecimal fromOverBegin) {
        this.fromOverBegin = fromOverBegin;
    }

    public BigDecimal getFromOverEnd() {
        return fromOverEnd;
    }

    public void setFromOverEnd(BigDecimal fromOverEnd) {
        this.fromOverEnd = fromOverEnd;
    }

    public BigDecimal getToOverBegin() {
        return toOverBegin;
    }

    public void setToOverBegin(BigDecimal toOverBegin) {
        this.toOverBegin = toOverBegin;
    }

    public BigDecimal getToOverEnd() {
        return toOverEnd;
    }

    public void setToOverEnd(BigDecimal toOverEnd) {
        this.toOverEnd = toOverEnd;
    }

    public long getAcceptTime() {
        return acceptTime;
    }

    public void setAcceptTime(long acceptTime) {
        this.acceptTime = acceptTime;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public String getToUserName() {
        return toUserName;
    }

    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }

    @Override
    public String getPlatformName() {
        return platformName;
    }

    @Override
    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public String getPlatformLogo() {
        return platformLogo;
    }

    public void setPlatformLogo(String platformLogo) {
        this.platformLogo = platformLogo;
    }

    @Override
    public String getCoinName() {
        return coinName;
    }

    @Override
    public void setCoinName(String coinName) {
        this.coinName = coinName;
    }

    public String getCoinLogo() {
        return coinLogo;
    }

    public void setCoinLogo(String coinLogo) {
        this.coinLogo = coinLogo;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

}
