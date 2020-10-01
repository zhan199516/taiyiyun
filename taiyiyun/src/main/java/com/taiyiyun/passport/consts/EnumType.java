package com.taiyiyun.passport.consts;

/**
 * Created by zhangjun on 2018/1/9.
 */
public enum EnumType {

    T1("类型1", 1),
    T2("类型2", 2),
    T3("类型3", 3);

    private String name ;
    private int index ;

    private EnumType(String name , int index ){
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
