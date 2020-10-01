package com.taiyiyun.passport.po.bchain;

import java.io.Serializable;

/**
 * Created by zhangjun on 2018/3/26.
 */
public class FoodProductDto implements Serializable {

    /*兑换积分*/
    private Integer rows;
    /*产品大约价值（RMB）*/
    private Long timestamp;

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
