package com.taiyiyun.passport.sqlserver.dao;

import com.taiyiyun.passport.sqlserver.po.Picture;

import java.util.List;

import org.apache.ibatis.annotations.Param;

/**
 * Created by nina on 2017/10/26.
 */
public interface IPictureDao {
    void deleteOldPicture(String entityId);
    void insertSelective(Picture picture);
    List<Picture> selectPictureByEntityId(String entityId);
	Picture getByEntityIdAndTypeId(@Param("entityId") String entityId, @Param("ptypeId") int ptypeId);
}
