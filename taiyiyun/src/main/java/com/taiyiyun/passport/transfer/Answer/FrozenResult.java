package com.taiyiyun.passport.transfer.Answer;

/**
 * Created by okdos on 2017/7/17.
 * 返回值：冻结，解冻结，转账返回值
 */
public class FrozenResult extends ErrorCodeResult {

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

    private Long froId;
    private String msg;

}
