package com.taiyiyun.passport.sqlserver.po;

/**
 * Created by nina on 2017/12/26.
 */
public class StatisticalData {

    //累计用户数
    private int allCount;
    //认证通过数
    private int PersionCount;
    //当日注册数
    private int TodayCount;

    private int height;
    private int txs;
    private int users;


    public int getAllCount() {
        return allCount;
    }

    public void setAllCount(int allCount) {
        this.allCount = allCount;
    }

    public int getPersionCount() {
        return PersionCount;
    }

    public void setPersionCount(int persionCount) {
        PersionCount = persionCount;
    }

    public int getTodayCount() {
        return TodayCount;
    }

    public void setTodayCount(int todayCount) {
        TodayCount = todayCount;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getTxs() {
        return txs;
    }

    public void setTxs(int txs) {
        this.txs = txs;
    }

    public int getUsers() {
        return users;
    }

    public void setUsers(int users) {
        this.users = users;
    }
}
