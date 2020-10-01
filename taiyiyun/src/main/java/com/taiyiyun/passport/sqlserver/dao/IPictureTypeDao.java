package com.taiyiyun.passport.sqlserver.dao;

import com.taiyiyun.passport.sqlserver.po.PictureType;

import java.util.List;

/**
 * Created by nina on 2017/10/23.
 */
public interface IPictureTypeDao {
    List<PictureType> getAllPictureTypes();
}
