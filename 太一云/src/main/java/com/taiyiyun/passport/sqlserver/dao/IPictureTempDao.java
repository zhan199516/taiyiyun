package com.taiyiyun.passport.sqlserver.dao;

import com.taiyiyun.passport.sqlserver.po.PictureTemp;

import java.util.Map;

/**
 * Created by nina on 2017/10/20.
 */
public interface IPictureTempDao {
	
    public PictureTemp getByUUID(String UUID);

    public PictureTemp getByUUIDAndPictureType(Map<String, Object> params);

    public int insert(PictureTemp pictureTemp);

    public int update(PictureTemp pictureTemp);
}
