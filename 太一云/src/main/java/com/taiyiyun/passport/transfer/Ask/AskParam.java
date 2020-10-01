package com.taiyiyun.passport.transfer.Ask;

import java.math.BigDecimal;


/**
 * Created by okdos on 2017/7/17.
 *  批量操作参数对象
 */
public class AskParam {

    /*请求冻结金额匹配key值*/
    private String mapkey;
    /*请求(冻结或转账)金额值*/
    private BigDecimal amount;
    /*冻结id*/
    private Long frozenId;
    /*转账手续费*/
    private BigDecimal charge;
    /*转账时目标用户Id*/
    private String toUserId;

    public String getMapkey() {
        return mapkey;
    }

    public void setMapkey(String mapkey) {
        this.mapkey = mapkey;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getFrozenId() {
        return frozenId;
    }

    public void setFrozenId(Long frozenId) {
        this.frozenId = frozenId;
    }

    public BigDecimal getCharge() {
        return charge;
    }

    public void setCharge(BigDecimal charge) {
        this.charge = charge;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

}
