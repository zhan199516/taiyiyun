package com.taiyiyun.passport.sqlserver.po;

/**
 * Created by nina on 2017/10/23.
 */
public class PictureType {
    private int PTypeID;
    private String PtypeName;
    private int EntityClass;

    public int getPTypeID() {
        return PTypeID;
    }

    public void setPTypeID(int PTypeID) {
        this.PTypeID = PTypeID;
    }

    public String getPtypeName() {
        return PtypeName;
    }

    public void setPtypeName(String ptypeName) {
        PtypeName = ptypeName;
    }

    public int getEntityClass() {
        return EntityClass;
    }

    public void setEntityClass(int entityClass) {
        EntityClass = entityClass;
    }
}
