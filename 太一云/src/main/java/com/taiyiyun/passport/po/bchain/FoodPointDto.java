package com.taiyiyun.passport.po.bchain;

import java.io.Serializable;

/**
 * Created by zhangjun on 2018/3/26.
 */
public class FoodPointDto implements Serializable {

    private Integer operType;
    private Integer rows;
    private Long timestamp;

    public Integer getOperType() {
        return operType;
    }

    public void setOperType(Integer operType) {
        this.operType = operType;
    }

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
