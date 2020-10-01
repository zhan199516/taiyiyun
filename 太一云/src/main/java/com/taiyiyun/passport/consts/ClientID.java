package com.taiyiyun.passport.consts;

/**
 * Created by zhangjun on 2018/1/9.
 */

/**
 * 客户端类型，存放各类链护照APP客户端id，如食品链、版权链等
 */
public enum ClientID {

    FWP("fwp-101",101),
    CWP("cwp-102", 102);

    private String id ;
    private int index ;

    private ClientID(String id , int index ){
        this.id = id ;
        this.index = index ;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
