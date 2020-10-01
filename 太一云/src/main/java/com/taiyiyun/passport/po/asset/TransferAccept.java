package com.taiyiyun.passport.po.asset;

import java.math.BigDecimal;

/**
 * Created by okdos on 2017/7/18.
 * 接受请求
 */
public class TransferAccept {
    private String tradeNo;
    private String toUuid;
    private String toUserId;
    private Long acceptTime;

    private BigDecimal fromOverBegin;
    private BigDecimal fromOverEnd;
    private BigDecimal toOverBegin;
    private BigDecimal toOverEnd;

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
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

    public String getToUuid() {
        return toUuid;
    }

    public void setToUuid(String toUuid) {
        this.toUuid = toUuid;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public Long getAcceptTime() {
        return acceptTime;
    }

    public void setAcceptTime(Long acceptTime) {
        this.acceptTime = acceptTime;
    }
}
