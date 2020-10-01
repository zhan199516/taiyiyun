package com.taiyiyun.passport.sqlserver.comm;

import com.taiyiyun.passport.init.SpringContext;
import com.taiyiyun.passport.sqlserver.dao.IPictureTypeDao;
import com.taiyiyun.passport.sqlserver.po.PictureType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nina on 2017/10/23.
 */
public class PictureTypeHolder {
    private static PictureTypeHolder ptHolder;
    private List<PictureType> pictureTypeList;
    private Map<Integer, PictureType> map = new HashMap<>();
    private PictureTypeHolder(){
        IPictureTypeDao ptDao = SpringContext.getBean(IPictureTypeDao.class);
        pictureTypeList = ptDao.getAllPictureTypes();
        if(pictureTypeList != null && pictureTypeList.size() > 0) {
            for(PictureType pt : pictureTypeList) {
                map.put(pt.getPTypeID(), pt);
            }
        }
    }
    public static PictureTypeHolder getInstance() {
        if(ptHolder == null) {
            synchronized (PictureTypeHolder.class) {
                if(ptHolder == null) {
                    ptHolder = new PictureTypeHolder();
                }
            }
        }
        return ptHolder;
    }
    public PictureType getPictureType(int PTypeID) {
        return map.get(PTypeID);
    }
}
