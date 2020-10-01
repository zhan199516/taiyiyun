package com.taiyiyun.passport.sqlserver.po;

import java.util.Date;

/**
 * Created by nina on 2017/10/20.
 */
public class PictureTemp {

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public int getPTypeID() {
        return PTypeID;
    }

    public void setPTypeID(int PTypeID) {
        this.PTypeID = PTypeID;
    }

    public String getFileName() {
        return FileName;
    }

    public void setFileName(String fileName) {
        FileName = fileName;
    }

    public int getFinished() {
        return Finished;
    }

    public void setFinished(int finished) {
        Finished = finished;
    }

    public Date getCreationTime() {
        return CreationTime;
    }

    public void setCreationTime(Date creationTime) {
        CreationTime = creationTime;
    }

    private String UUID;
    private int PTypeID;
    private String FileName;
    private int Finished;
    private Date CreationTime;

}
