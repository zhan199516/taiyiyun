package com.taiyiyun.passport.consts;

/**
 * Created by zhangjun on 2018/1/9.
 */
public enum EnumStatus {

    ZORO("成功", 0),
    ONE("失败状态", 1),
    TWO("失败状态", 2),
    THREE("失败状态", 3),
    FOUR("失败状态", 4),
    FIVE("失败状态", 5),
    SIX("失败状态", 6),
    SEVEN("失败状态", 7),
    EIGHT("失败状态", 8),
    NINE("失败状态", 9),
    TEN("失败状态", 10),
    ELEVEN("失败状态", 11),
    TWELVE("失败状态", 12),
    NINETY_SEVEN("获取token失败", 98),
    NINETY_EIGHT("未登录", 98),
    NINETY_NINE("系统错误", 99);

    private String name ;
    private int index ;

    private EnumStatus(String name , int index ){
        this.name = name ;
        this.index = index ;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
    }
}
