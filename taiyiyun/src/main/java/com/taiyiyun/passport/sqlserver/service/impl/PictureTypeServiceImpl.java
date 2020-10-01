package com.taiyiyun.passport.sqlserver.service.impl;

import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taiyiyun.passport.sqlserver.dao.IPictureTypeDao;
import com.taiyiyun.passport.sqlserver.po.PictureType;
import com.taiyiyun.passport.sqlserver.service.IPictureTypeService;

@Service
public class PictureTypeServiceImpl implements IPictureTypeService {
	
	@Autowired
	private IPictureTypeDao dao;

	@Override
	public boolean existPictureType(String typeName) {
		List<PictureType> typeList = dao.getAllPictureTypes();
		if (typeList != null && typeList.size() > 0) {
			for (Iterator<PictureType> it = typeList.iterator(); it.hasNext(); ) {
				PictureType bean = it.next();
				if (typeName.equals(bean.getPTypeID())) {
					return true;
				}
			}
		}
		return false;
	}

}
