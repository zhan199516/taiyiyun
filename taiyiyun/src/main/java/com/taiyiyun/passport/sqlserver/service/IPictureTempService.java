package com.taiyiyun.passport.sqlserver.service;

import com.taiyiyun.passport.sqlserver.comm.PictureType;
import com.taiyiyun.passport.sqlserver.po.PictureTemp;

public interface IPictureTempService {

	public PictureTemp getPictureTemp(String uuid, PictureType pictureType);

	public void saveFile(String uuid, PictureTemp pictureTemp, String base64File, PictureType pictureType) throws Exception;

	public void saveFile(String uuid, PictureTemp pictureTempFront, String idCardPhotoBase64Front, PictureTemp pictureTempBack, String idCardPhotoBase64Back) throws Exception;

}
