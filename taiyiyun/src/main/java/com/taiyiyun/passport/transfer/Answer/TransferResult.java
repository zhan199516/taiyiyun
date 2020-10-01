package com.taiyiyun.passport.transfer.Answer;

import java.math.BigDecimal;

/**
 * Created by okdos on 2017/7/22.
 * 转账返回信息
 */
public class TransferResult extends ErrorCodeResult {
    private Long froId;
    private String msg;
    //转账前from的余额
    private BigDecimal fromoverFront;
    //转账后from的余额
    private BigDecimal fromover;
    //转帐前to的余额
    private BigDecimal tooverFront;
    //转账后to的余额
    private BigDecimal toover;

    public Long getFroId() {
        return froId;
    }

    public void setFroId(Long froId) {
        this.froId = froId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public BigDecimal getFromoverFront() {
        return fromoverFront;
    }

    public void setFromoverFront(BigDecimal fromoverFront) {
        this.fromoverFront = fromoverFront;
    }

    public BigDecimal getFromover() {
        return fromover;
    }

    public void setFromover(BigDecimal fromover) {
        this.fromover = fromover;
    }

    public BigDecimal getTooverFront() {
        return tooverFront;
    }

    public void setTooverFront(BigDecimal tooverFront) {
        this.tooverFront = tooverFront;
    }

    public BigDecimal getToover() {
        return toover;
    }

    public void setToover(BigDecimal toover) {
        this.toover = toover;
    }
}
