package com.taiyiyun.passport.transfer.Answer;

import java.math.BigDecimal;

/**
 * Created by okdos on 2017/7/17.
 * 获取费用信息
 */
public class AskFeeResult extends ErrorCodeResult {

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(BigDecimal totalFee) {
        this.totalFee = totalFee;
    }

    private BigDecimal fee;
    private BigDecimal balance;
    private BigDecimal totalFee;

}
