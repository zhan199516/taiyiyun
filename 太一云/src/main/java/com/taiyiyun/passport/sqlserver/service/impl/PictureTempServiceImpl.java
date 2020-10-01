package com.taiyiyun.passport.sqlserver.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.taiyiyun.passport.commons.PassPortUtil;
import com.taiyiyun.passport.sqlserver.comm.PictureType;
import com.taiyiyun.passport.sqlserver.dao.IPictureTempDao;
import com.taiyiyun.passport.sqlserver.po.PictureTemp;
import com.taiyiyun.passport.sqlserver.service.IPictureTempService;
import com.taiyiyun.passport.util.StringUtil;

@Service
public class PictureTempServiceImpl implements IPictureTempService {
	
	@Autowired
	private IPictureTempDao pictureTempDao;

	@Override
	public PictureTemp getPictureTemp(String uuid, PictureType pictureType) {
		if (StringUtil.isEmpty(uuid) || pictureType == null) {
			return null;
		}
		
		Map<String, Object> params = new HashMap<>();
        params.put("UUID", uuid);
        params.put("PTypeID", pictureType.getPtypeId());
        return pictureTempDao.getByUUIDAndPictureType(params);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, transactionManager = "transactionManagerSqlserver", rollbackFor = Exception.class)
	public void saveFile(String uuid, PictureTemp pictureTemp, String base64File, PictureType pictureType) throws Exception {
		if (StringUtil.isEmpty(uuid) || StringUtil.isEmpty(base64File) || pictureType == null) {
			throw new NullPointerException("uuid is null or base64File is null or pictureType is null");
		}
		
		String filePath = PassPortUtil.saveIdCardFront(base64File, pictureTemp == null ? null : pictureTemp.getFileName());
		
		if (pictureTemp == null) {
			pictureTemp = new PictureTemp();
			pictureTemp.setUUID(uuid);
			pictureTemp.setFileName(filePath);
			pictureTemp.setFinished(0);
			pictureTemp.setPTypeID(pictureType.getPtypeId());
			pictureTemp.setCreationTime(new Date());
			int count = pictureTempDao.insert(pictureTemp);
			if (count != 1) {
				throw new Exception("PictureTemp data save error");
			}
		} else {
			pictureTemp.setCreationTime(new Date());
			int count = pictureTempDao.update(pictureTemp);
			if (count != 1) {
				throw new Exception("PictureTemp data update error");
			}
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, transactionManager = "transactionManagerSqlserver", rollbackFor = Exception.class) 
	public void saveFile(String uuid, PictureTemp pictureTempFront, String idCardPhotoBase64Front, PictureTemp pictureTempBack, String idCardPhotoBase64Back) throws Exception {
		if (StringUtil.isEmpty(uuid) || StringUtil.isEmpty(idCardPhotoBase64Front) || StringUtil.isEmpty(idCardPhotoBase64Back)) {
			throw new NullPointerException("params is null");
		}
		
		saveFile(uuid, pictureTempFront, idCardPhotoBase64Front, PictureType.IDCARD_FRONT);
		saveFile(uuid, pictureTempBack, idCardPhotoBase64Back, PictureType.IDCARD_BACK);
		
	}

}
