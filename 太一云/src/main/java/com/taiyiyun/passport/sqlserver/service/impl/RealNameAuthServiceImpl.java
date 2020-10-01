package com.taiyiyun.passport.sqlserver.service.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import sun.misc.BASE64Decoder;

import com.taiyiyun.passport.commons.PassPortUtil;
import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.exception.router.RouterException;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.sqlserver.comm.PictureType;
import com.taiyiyun.passport.sqlserver.dao.IDeveloperDao;
import com.taiyiyun.passport.sqlserver.dao.IEntityDao;
import com.taiyiyun.passport.sqlserver.dao.IEntityDetailDao;
import com.taiyiyun.passport.sqlserver.dao.IPictureDao;
import com.taiyiyun.passport.sqlserver.dao.IPictureTempDao;
import com.taiyiyun.passport.sqlserver.dao.IUserEntityDao;
import com.taiyiyun.passport.sqlserver.dao.IUsersDao;
import com.taiyiyun.passport.sqlserver.po.CustomerDetailPerson;
import com.taiyiyun.passport.sqlserver.po.Developer;
import com.taiyiyun.passport.sqlserver.po.Entity;
import com.taiyiyun.passport.sqlserver.po.EntityDetail;
import com.taiyiyun.passport.sqlserver.po.Picture;
import com.taiyiyun.passport.sqlserver.po.PictureTemp;
import com.taiyiyun.passport.sqlserver.po.UserEntity;
import com.taiyiyun.passport.sqlserver.po.Users;
import com.taiyiyun.passport.sqlserver.service.IRealNameAuthService;
import com.taiyiyun.passport.util.DateUtil;
import com.taiyiyun.passport.util.FileUtil;
import com.taiyiyun.passport.util.StringUtil;

/**
 * Created by nina on 2017/10/24.
 */
@Service
public class RealNameAuthServiceImpl implements IRealNameAuthService {
    private Log log = LogFactory.getLog(RealNameAuthServiceImpl.class);
    @Resource
    private IUsersDao usersDao;
    @Resource
    private IPictureTempDao pictureTempDao;
    @Resource
    private IEntityDao entityDao;
    @Resource
    private IDeveloperDao developerDao;
    @Resource
    private IUserEntityDao userEntityDao;
    @Resource
    private IEntityDetailDao entityDetailDao;
    @Resource
    private IPictureDao pictureDao;

    @Override
    public Users getUserFromAddress(String Address) {
        return usersDao.getUserFromAddress(Address);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void insertPictureTemp(PictureTemp pictureTemp) {
        pictureTempDao.insert(pictureTemp);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void updatePictureTemp(PictureTemp pictureTemp) {
        pictureTempDao.update(pictureTemp);
    }

    @Override
    public PictureTemp getPictureTemp(String UUID, Integer PTypeID) {
        Map<String, Object> params = new HashMap<>();
        params.put("UUID", UUID);
        params.put("PTypeID", PTypeID);
        return pictureTempDao.getByUUIDAndPictureType(params);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED , transactionManager="transactionManagerSqlserver")
    public Map<String, Object> addCustomerPerson(CustomerDetailPerson cdp, PackBundle bundle, String facePhotoBase64) throws IOException {
        Map<String, Object> result = new HashMap<>();
        //检查身份证号和护照号是否为空
        if(StringUtils.isEmpty(cdp.getIDCard()) && StringUtils.isEmpty(cdp.getPassport())) {
            result.put("success", false);
            result.put("error", bundle.getString("authenticate.error.msg.idcardandpassport"));
            return result;
        }
        Users myUser = usersDao.getUserFromAddress(cdp.getAddress());
        //检查用户
        if(myUser == null) {
            result.put("success", false);
            result.put("error", bundle.getString("authenticate.error.msg.usernotfound"));
            return result;
        }
        //检查身份证
        if(StringUtils.isNotEmpty(cdp.getIDCard())) {
            Map<String, String> params = new HashMap<>();
            params.put("idCard", cdp.getIDCard());
            params.put("uuid", myUser.getUUID());
            List<Entity> entities = entityDao.selectEntitiesByIDCardAndUUID(params);
            if(entities != null && entities.size() > 0) {
                result.put("success", false);
                result.put("error", bundle.getString("id.number.exists"));
                return result;
            }
        }
        //检查护照号(因为护照号未采用，所以这里不用校验护照号)
        //查找Developer
        Developer developer = developerDao.selectDeveloperByAppKey(cdp.getAppKey());
        if(developer == null) {
            result.put("success", false);
            result.put("error", bundle.getString("authenticate.error.msg.appkeyparam"));
            return result;
        }
        //添加EntityDetail
        String oldEntityId = "";
        //判断是否已经有个人身份
        List<Entity> entities = entityDao.selectEntitysByUUID(myUser.getUUID());
        if(entities != null && entities.size() > 0) {
            if(entities.get(0).getStatus() == 1) {
                result.put("success", false);
                result.put("error", bundle.getString("user.has.checked"));
                return result;
            } else {
                oldEntityId = entities.get(0).getEntityId();
            }
        }
        //清除未审核或审核失败的信息
        if(oldEntityId.length() > 0) {
            entityDetailDao.deleteEndityDetailByEntityId(oldEntityId);
            entityDao.deleteEntityByEntityId(oldEntityId);
            Map<String, String> params = new HashMap<>();
            params.put("entityId", oldEntityId);
            params.put("uuid", myUser.getUUID());
            userEntityDao.deleteUserEntityByEntityIdAndUUID(params);
        }
        //保存Entity/UserEntity/EntityDetail
        //1.Entity
        Entity entity = new Entity();
        entity.setEntityId(StringUtil.getUUID());
        entity.setEntityUnqueId(StringUtils.isEmpty(cdp.getIDCard()) ? cdp.getPassport() : cdp.getIDCard());
        Date now = new Date();
        entity.setCreationTime(now);
        entity.setTypeId(1);
        entity.setEntityName(cdp.getEntityName());
        entity.setStatus(1);
        entityDao.insertSelective(entity);
        //2.UserEntity
        UserEntity userEntity = new UserEntity();
        userEntity.setEntityId(entity.getEntityId());
        userEntity.setStatus(1);
        userEntity.setUuid(myUser.getUUID());
        userEntity.setUserEntityId(StringUtil.getUUID());
        userEntity.setDefaultAmount(10000);
        userEntity.setAuditChannel(2);
        userEntityDao.insertSelective(userEntity);
        //3.EntityDetail
        List<EntityDetail> entityDetails = new ArrayList<>();
        makeEntityDetails(cdp, entityDetails, entity.getEntityId());
        if(entityDetails != null && entityDetails.size() > 0) {
            for(EntityDetail ed : entityDetails) {
                entityDetailDao.insertSelective(ed);
            }
        }
        //4.空身份禁用
        entityDao.disableEntityStatusZero(myUser.getUUID());
        userEntityDao.disableUserEntityStatusZero(myUser.getUUID());
        //5.更新默认实体状态
        if(userEntity.getStatus() == 1) {
            Map<String, String> params = new HashMap<>();
            params.put("userEntityId", userEntity.getUserEntityId());
            params.put("uuid", userEntity.getUuid());
            usersDao.updateDefaultUserEntity(params);
        }
        //处理头像信息
        //保存头像图片及相关信息存入数据库
        String root = Config.get("uploadpath");
        String filePath = DateUtil.toString(now, "yyyy") + File.separator + DateUtil.toString(now, "MM") + File.separator + DateUtil.toString(now, "dd") + File.separator + StringUtil.getUUID() + ".jpg";
        String myfile = root + filePath;
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            byte[] bytes = decoder.decodeBuffer(facePhotoBase64);
            FileUtil.copyTo(bytes, myfile);
            //保存头像数据到数据库
            //1)删除旧的图片数据
            pictureDao.deleteOldPicture(entity.getEntityId());
            //2)保存头像数据
            Picture pic = new Picture();
            pic.setEntityId(entity.getEntityId());
            pic.setCreationTime(now);
            pic.setIsPublic(0);
            pic.setFileName(filePath);
            pic.setpTypeId(PictureType.YUN_SIGN_PHOTO.getPtypeId());
            pic.setPid(StringUtil.getUUID());
            pictureDao.insertSelective(pic);
            //将头像信息与身份证正面信息关联
            Map<String, Object> params = new HashMap<>();
            params.put("UUID", myUser.getUUID());
            params.put("PTypeID", PictureType.IDCARD_FRONT.getPtypeId());
            PictureTemp ptModel = pictureTempDao.getByUUIDAndPictureType(params);
            if(ptModel != null) {
                Picture picture = new Picture();
                picture.setCreationTime(now);
                picture.setpTypeId(PictureType.IDCARD_FRONT.getPtypeId());
                picture.setEntityId(entity.getEntityId());
                picture.setFileName(ptModel.getFileName());
                picture.setIsPublic(0);
                int start = ptModel.getFileName().lastIndexOf("\\") + 1;
                int end = ptModel.getFileName().lastIndexOf("\\") + 32;
                picture.setPid(ptModel.getFileName().substring(start, end));
                pictureDao.insertSelective(picture);
                ptModel.setFinished(1);
                pictureTempDao.update(ptModel);
            }
            //将头像信息与身份证反面信息关联
            params.put("PTypeID", PictureType.IDCARD_BACK.getPtypeId());
            PictureTemp ptModelBack = pictureTempDao.getByUUIDAndPictureType(params);
            if(ptModelBack != null) {
                Picture pictureBack = new Picture();
                pictureBack.setCreationTime(now);
                pictureBack.setpTypeId(PictureType.IDCARD_BACK.getPtypeId());
                pictureBack.setEntityId(entity.getEntityId());
                pictureBack.setFileName(ptModelBack.getFileName());
                pictureBack.setIsPublic(0);
                int start = ptModelBack.getFileName().lastIndexOf("\\") + 1;
                int end = ptModelBack.getFileName().lastIndexOf("\\") + 32;
                pictureBack.setPid(ptModelBack.getFileName().substring(start, end));
                pictureDao.insertSelective(pictureBack);
                ptModelBack.setFinished(1);
                pictureTempDao.update(ptModelBack);
            }
        } catch (IOException e) {
            log.error("保存用户认证用的扫描头像图片失败：" + e.getMessage());
            throw e;
        }
        result.put("success", true);
        result.put("error", null);
        result.put("data", entity);
        result.put("errorCode", null);
        return result;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, transactionManager="transactionManagerSqlserver")
    public Map<String, Object> fillUpRealNameInfo(String idCardPhotoBase64Back, String address, String identityNumber, String userName, String validDateStart, String validDateEnd, String sex, String nation, String birthDay, String homeAddr, PackBundle bundle) {
        Map<String, Object> result = new HashMap<>();
        result.put("data", null);
        result.put("success", true);
        result.put("error", "");
        result.put("errorCode", null);
        Users user = usersDao.getUserFromAddress(address);
        if(user == null) {
            result.put("success", false);
            result.put("error", bundle.getString("authenticate.error.msg.usernotfound"));
            return result;
        }
        //验证上传的信息是否是同一张身份证
        Entity entity = entityDao.selectAuthedEntityByUUID(user.getUUID());
        if(!(StringUtils.equalsIgnoreCase(entity.getEntityUnqueId(), identityNumber) && StringUtils.equalsIgnoreCase(entity.getEntityName(), userName))) {
            result.put("success", false);
            result.put("error", bundle.getString("authenticate.error.msg.notthesame"));
            return result;
        }

        //保存身份证背面照
        PictureTemp pictureTemp = getPictureTemp(user.getUUID(), 2);
        if (pictureTemp != null && pictureTemp.getFinished() == 1) {
            result.put("success", false);
            result.put("error", bundle.getString("picture.modify.deny.register"));
            return result;
        }
        String filePath;
        try {
            String oldPath = null;
            if(pictureTemp != null) {
                oldPath = pictureTemp.getFileName();
            }
            filePath = PassPortUtil.saveIdCardBack(idCardPhotoBase64Back, oldPath);
        } catch (IOException e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", bundle.getString("photo.file.save.failed"));
            return result;
        }
        if (pictureTemp == null) {
            pictureTemp = new PictureTemp();
            pictureTemp.setCreationTime(new Date());
            pictureTemp.setFileName(filePath);
            pictureTemp.setFinished(0);
            pictureTemp.setPTypeID(2);
            pictureTemp.setUUID(user.getUUID());
            insertPictureTemp(pictureTemp);
        } else {
            pictureTemp.setCreationTime(new Date());
            updatePictureTemp(pictureTemp);
        }
        //身份证反面信息关联
        saveIdCardBack(user, entity, pictureTemp);

        //保存非照片信息
        Map<String, String> params = new HashMap<> ();
        params.put("mobilePrefix", user.getMobilePrefix());
        params.put("mobile", user.getMobile());
        EntityDetail ed = entityDetailDao.selectEntityDetailHasValidDateByMobile(params);
        if(ed == null) {
            List<EntityDetail> edList = new ArrayList<>();
            CustomerDetailPerson cdp = new CustomerDetailPerson();
            cdp.setBirthDay(birthDay);
            cdp.setSex(sex);
            cdp.setNation(nation);
            cdp.setHomeAddr(homeAddr);
            cdp.setValidDateEnd(validDateEnd);
            cdp.setValidDateStart(validDateStart);
            makeEntityDetails(cdp, edList, entity.getEntityId());
            if(!edList.isEmpty()) {
                for(EntityDetail entityDetail : edList) {
                    entityDetailDao.insertSelective(entityDetail);
                }
            }
        }
        return result;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, transactionManager="transactionManagerSqlserver")
    public void addOtherCustomerPerson(CustomerDetailPerson cdp) {
        log.info("====================myUser=====================");
        Users myUser = usersDao.getUserFromAddress(cdp.getAddress());
        log.info("nikeName = " + myUser.getNikeName() + "\r\n");
        log.info("====================entity=====================\r\n");
        Entity entity = entityDao.selectAuthedEntityByUUID(myUser.getUUID());
        log.info("entityName = " + entity.getEntityName() + "\r\n");
        if(entity != null) {
            List<EntityDetail> entityDetails = new ArrayList<>();
            makeEntityDetails(cdp, entityDetails, entity.getEntityId());
            if(entityDetails != null && entityDetails.size() > 0) {
                for(EntityDetail ed : entityDetails) {
                    entityDetailDao.insertSelective(ed);
                }
            }
            Map<String, Object> params = new HashMap<>();
            params.put("PTypeID", PictureType.IDCARD_BACK.getPtypeId());
            params.put("UUID", myUser.getUUID());
            log.info("========================PictureTemp===========================");
            PictureTemp ptModelBack = pictureTempDao.getByUUIDAndPictureType(params);
            log.info("fileName = " + ptModelBack.getFileName());
            //身份证反面信息关联
            saveIdCardBack(myUser, entity, ptModelBack);
        }
    }

    private void saveIdCardBack(Users myUser, Entity entity, PictureTemp ptModelBack) {
        if(ptModelBack != null) {
            Picture pictureBack = new Picture();
            pictureBack.setCreationTime(new Date());
            pictureBack.setEntityId(entity.getEntityId());
            pictureBack.setFileName(ptModelBack.getFileName());
            pictureBack.setIsPublic(0);
            pictureBack.setpTypeId(2);
            int start = ptModelBack.getFileName().lastIndexOf("\\") + 1;
            int end = ptModelBack.getFileName().lastIndexOf("\\") + 32;
            pictureBack.setPid(ptModelBack.getFileName().substring(start, end));
            pictureDao.insertSelective(pictureBack);
            ptModelBack.setFinished(1);
            pictureTempDao.update(ptModelBack);
        }
    }

    @Override
    public EntityDetail queryEntityDetailHasValidDateByMobile(String mobile, String mobilePrefix) {
        Map<String, String> params = new HashMap<>();
        params.put("mobile", mobile);
        params.put("mobilePrefix", mobilePrefix);
        return entityDetailDao.selectEntityDetailHasValidDateByMobile(params);
    }

    @Override
    public Entity queryEntityIsEnabledByUUID(String UUID) {
        return entityDao.selectAuthedEntityByUUID(UUID);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void uploadInfoForManualReview(String appKey, String address, String name, String idCard, String passport, String sign, String country, String countryCode, String validDateStart, String validDateEnd, String sex, String nation, String birthDay, String homeAddr, MultipartFile fileCardFront, MultipartFile fileCardBack, MultipartFile fileHandCard, MultipartFile fileHomeAddr) {
        Users user = usersDao.getUserFromAddress(address);
        if(user == null) {
            throw new RouterException("user.address.fault", "用户地址错误", null);
        }
        //验证身份证号是否已经存在
        if(StringUtils.isNotEmpty(idCard)) {
            Map<String, String> params = new HashMap<>();
            params.put("idCard", idCard);
            params.put("uuid", user.getUUID());
            List<Entity> entities = entityDao.selectEntitiesByIDCardAndUUID(params);
            if(entities != null && !entities.isEmpty()) {
                throw new RouterException("id.number.exists", "身份证号码已经存在", null);
            }
        }
        //验证护照号是否已经存在
        if(StringUtils.isNotEmpty(passport)) {
            Map<String, String> params = new HashMap<>();
            params.put("idCard", passport);
            params.put("uuid", user.getUUID());
            List<Entity> entities = entityDao.selectEntitiesByIDCardAndUUID(params);
            if(entities != null && !entities.isEmpty()) {
                throw new RouterException("id.number.exists", "护照号码已存在", null);
            }
        }
        Map<String, Object> params = new HashMap<>();
        params.put("uuid", user.getUUID());
        params.put("typeId", 1);
        List<UserEntity> userEntities = userEntityDao.selectUserEntityByUUIDAndTypeId(params);
        List<Picture> oldPictures = null;
        if(userEntities != null && userEntities.size() > 0) {
            if(userEntities.get(0).getStatus() == 1) {
                throw new RouterException("", "用户已有个人身份", null);
            }
            //存储图片文件，如果已经存在原图片将原图片替换
            oldPictures = pictureDao.selectPictureByEntityId(userEntities.get(0).getEntityId());
        }
        String fileCardFrontOldName = "";
        String fileCardBackOldName = "";
        String fileHandCardOldName = "";
        String fileHomeAddrOldName = "";
        if(oldPictures != null && oldPictures.size() > 0) {
            for(int i = 0; i < oldPictures.size(); i++) {
                Picture pic = oldPictures.get(i);
                int ptypeId = pic.getpTypeId();
                if(ptypeId == 1 || ptypeId == 8) {//证件正面照
                    fileCardFrontOldName = pic.getFileName();
                } else if(ptypeId == 2 || ptypeId == 15) { //证件反面
                    fileCardBackOldName = pic.getFileName();
                } else if(ptypeId == 3 || ptypeId == 9) {//证件手持
                    fileHandCardOldName = pic.getFileName();
                } else if(ptypeId == 14) {//住址证明
                    fileHomeAddrOldName = pic.getFileName();
                }
            }
        }
        String fileCardFrontName = "";
        String fileCardBackName = "";
        String fileHandCardName = "";
        String fileHomeAddrName = "";
        try {
            fileCardFrontName = PassPortUtil.savePicture(fileCardFront, fileCardFrontOldName);
            fileCardBackName = PassPortUtil.savePicture(fileCardBack, fileCardBackOldName);
            fileHandCardName = PassPortUtil.savePicture(fileHandCard, fileHandCardOldName);
            fileHomeAddrName = PassPortUtil.savePicture(fileHomeAddr, fileHomeAddrOldName);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RouterException("", "保存手工认证审核图片出现异常", null);
        }
        String  oldEntityId = "";
        List<Entity> entities = entityDao.selectEntitysByUUID(user.getUUID());
        //判断是否有个人身份
        if(entities != null && entities.size() > 0 && entities.get(0).getStatus() == 1) {
            throw new RouterException("", "用户已有个人身份", null);
        } else {
            oldEntityId = userEntities != null && userEntities.size() > 0 ? userEntities.get(0).getEntityId() : "";
        }
        //清除未审核或审核失败的信息
        if(oldEntityId.length() > 0) {
            entityDetailDao.deleteEndityDetailByEntityId(oldEntityId);
            entityDao.deleteEntityByEntityId(oldEntityId);
            Map<String, String> params1 = new HashMap<>();
            params1.put("entityId", oldEntityId);
            params1.put("uuid", user.getUUID());
            userEntityDao.deleteUserEntityByEntityIdAndUUID(params1);
        }
        CustomerDetailPerson cdp = new CustomerDetailPerson();
        cdp.setAppKey(appKey);
        cdp.setAddress(address);
        cdp.setIDCard(idCard);
        cdp.setEntityName(name);
        cdp.setName(name);
        cdp.setValidDateStart(validDateStart);
        cdp.setValidDateEnd(validDateEnd);
        cdp.setNation(nation);
        cdp.setSex(sex);
        cdp.setHomeAddr(homeAddr);
        cdp.setBirthDay(birthDay);
        cdp.setPassport(passport);
        cdp.setCountry(country);
        cdp.setCountryCode(countryCode);
        //保存Entity
        Entity entity1 = new Entity();
        entity1.setEntityId(StringUtil.getUUID());
        entity1.setEntityUnqueId(StringUtils.isEmpty(cdp.getIDCard()) ? cdp.getPassport() : cdp.getIDCard());
        Date now = new Date();
        entity1.setCreationTime(now);
        entity1.setTypeId(1);
        entity1.setEntityName(cdp.getEntityName());
        entity1.setStatus(0);
        entityDao.insertSelective(entity1);
        //2.UserEntity
        UserEntity userEntity = new UserEntity();
        userEntity.setEntityId(entity1.getEntityId());
        userEntity.setStatus(0);
        userEntity.setUuid(user.getUUID());
        userEntity.setUserEntityId(StringUtil.getUUID());
        userEntity.setDefaultAmount(10000);
        userEntityDao.insertSelective(userEntity);
        //3.EntityDetail
        List<EntityDetail> entityDetails = new ArrayList<>();
        makeEntityDetails(cdp, entityDetails, entity1.getEntityId());
        if(entityDetails != null && entityDetails.size() > 0) {
            for(EntityDetail ed : entityDetails) {
                entityDetailDao.insertSelective(ed);
            }
        }
        //4.空身份禁用
        entityDao.disableEntityStatusZero(user.getUUID());
        userEntityDao.disableUserEntityStatusZero(user.getUUID());
        //5.处理图像
        Map<Integer, String> mapName = new HashMap<>();
        if(StringUtils.isNotEmpty(idCard)) {
            mapName.put(1, fileCardFrontName);
            mapName.put(2, fileCardBackName);
            mapName.put(3, fileHandCardName);
        } else {
            mapName.put(8, fileCardFrontName);
            mapName.put(15, fileCardBackName);
            mapName.put(9, fileHandCardName);
        }
        mapName.put(14, fileHomeAddrName);
        for(Map.Entry<Integer, String> entry : mapName.entrySet()) {
            Picture pic = new Picture();
            pic.setEntityId(entity1.getEntityId());
            pic.setCreationTime(now);
            pic.setIsPublic(0);
            pic.setFileName(entry.getValue());
            pic.setpTypeId(entry.getKey());
            pic.setPid(StringUtil.getUUID());
            pictureDao.insertSelective(pic);
        }
        //6.更新默认实体状态
        if(userEntity.getStatus() == 1) {
            Map<String, String> params1 = new HashMap<>();
            params1.put("userEntityId", userEntity.getUserEntityId());
            params1.put("uuid", userEntity.getUuid());
            usersDao.updateDefaultUserEntity(params1);
        }
    }

    public static void main(String[] args) {
        String pic = "2017\\1\\19\\001aa422cade4dd89b031fe7423af977.jpg";
        System.out.println(pic.substring(pic.lastIndexOf("\\")+1, pic.lastIndexOf("\\")+32));
    }

    private void makeEntityDetails(CustomerDetailPerson cdp, List<EntityDetail> entityDetails, String entityId) {
        Field[] fields = CustomerDetailPerson.class.getDeclaredFields();
        for(Field field : fields) {
            try {
                field.setAccessible(true);
                if(field.get(cdp) != null && !StringUtils.equalsIgnoreCase("address", field.getName())) {
                    EntityDetail ed = new EntityDetail();
                    ed.setEntityId(entityId);
                    ed.setStatus(1);
                    ed.setInfoKey(field.getName());
                    ed.setInfoValue(field.get(cdp).toString());
                    ed.setIsPublic(0);
                    entityDetails.add(ed);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

	@Override
    @Transactional(propagation = Propagation.REQUIRED , transactionManager="transactionManagerSqlserver", rollbackFor = Exception.class)
	public Map<String, Object> addCustomerPerson(CustomerDetailPerson cdp, Users users, PackBundle bundle, Map<Integer, String> pictures, int status) {
		if (cdp == null || users == null || bundle == null || status < 0) {
			throw new IllegalArgumentException("params is illegal");
		}
		
		Map<String, Object> result = new HashMap<String, Object>();
		
		//检查身份证号和护照号是否为空
        if (StringUtils.isEmpty(cdp.getIDCard()) && StringUtils.isEmpty(cdp.getPassport())) {
            result.put("success", false);
            result.put("error", bundle.getString("authenticate.error.msg.idcardandpassport"));
            return result;
        }
       
        //检查身份证,证件号已经被注册并且审核通过
        if (StringUtils.isNotEmpty(cdp.getIDCard())) {
            Map<String, String> params = new HashMap<>();
            params.put("idCard", cdp.getIDCard());
            params.put("uuid", users.getUUID());
            List<Entity> entities = entityDao.selectEntitiesByIDCardAndUUID(params);
            if (entities != null && entities.size() > 0) {
                result.put("success", false);
                result.put("error", bundle.getString("id.number.exists"));
                return result;
            }
        }
        //检查护照号(因为护照号未采用，所以这里不用校验护照号)
        
        //添加EntityDetail
        String oldEntityId = "";
        //判断是否已经有个人身份
        List<Entity> entities = entityDao.selectEntitysByUUID(users.getUUID());
        if (entities != null && entities.size() > 0) {
            if(entities.get(0).getStatus() == 1) {
                result.put("success", false);
                result.put("error", bundle.getString("user.has.checked"));
                return result;
            } else {
                oldEntityId = entities.get(0).getEntityId();
            }
        }
        
        //清除未审核或审核失败的信息
        if (StringUtil.isNotEmpty(oldEntityId)) {
            entityDetailDao.deleteEndityDetailByEntityId(oldEntityId);
            entityDao.deleteEntityByEntityId(oldEntityId);
            Map<String, String> params = new HashMap<>();
            params.put("entityId", oldEntityId);
            params.put("uuid", users.getUUID());
            userEntityDao.deleteUserEntityByEntityIdAndUUID(params);
            pictureDao.deleteOldPicture(oldEntityId);
        }
        
        //保存Entity/UserEntity/EntityDetail
        //1.Entity
        Entity entity = new Entity();
        entity.setEntityId(StringUtil.getUUID());
        entity.setEntityUnqueId(StringUtils.isEmpty(cdp.getIDCard()) ? cdp.getPassport() : cdp.getIDCard());
        Date now = new Date();
        entity.setCreationTime(now);
        entity.setTypeId(1);
        entity.setEntityName(cdp.getEntityName());
        entity.setStatus(status);
        entityDao.insertSelective(entity);
        
        //2.UserEntity
        UserEntity userEntity = new UserEntity();
        userEntity.setEntityId(entity.getEntityId());
        userEntity.setStatus(status);
        userEntity.setUuid(users.getUUID());
        userEntity.setUserEntityId(StringUtil.getUUID());
        userEntity.setDefaultAmount(10000);
        userEntity.setAuditChannel(2);
        userEntityDao.insertSelective(userEntity);
      
        //3.EntityDetail
        List<EntityDetail> entityDetails = new ArrayList<>();
        makeEntityDetails(cdp, entityDetails, entity.getEntityId());
        if (entityDetails != null && entityDetails.size() > 0) {
            for(EntityDetail ed : entityDetails) {
                entityDetailDao.insertSelective(ed);
            }
        }
        
        //4.空身份禁用
        entityDao.disableEntityStatusZero(users.getUUID());
        userEntityDao.disableUserEntityStatusZero(users.getUUID());
        //5.更新默认实体状态
        if (userEntity.getStatus() == 1) {
            Map<String, String> params = new HashMap<>();
            params.put("userEntityId", userEntity.getUserEntityId());
            params.put("uuid", userEntity.getUuid());
            usersDao.updateDefaultUserEntity(params);
        }
        
        if (pictures != null && pictures.size() > 0) {
        	Iterator<Integer> it = pictures.keySet().iterator();
        	while (it.hasNext()) {
        		Integer pTypeId = it.next();
        		String fileName = pictures.get(pTypeId);
        		
        		Picture picture = new Picture();
        		picture.setCreationTime(new Date());
        		picture.setEntityId(entity.getEntityId());
        		picture.setFileName(fileName);
        		picture.setIsPublic(0);
        		picture.setpTypeId(pTypeId);
        		
        		int start = fileName.lastIndexOf("\\") + 1;
                int end = fileName.lastIndexOf("\\") + 32;
                picture.setPid(fileName.substring(start, end));
                pictureDao.insertSelective(picture);
			}
        }
        
        result.put("success", true);
        result.put("error", null);
        result.put("data", entity.getEntityId());
        result.put("errorCode", null);
		return result;
	}

}