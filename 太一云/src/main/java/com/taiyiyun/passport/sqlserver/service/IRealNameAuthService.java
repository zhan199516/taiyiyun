package com.taiyiyun.passport.sqlserver.service;

import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.sqlserver.po.*;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Created by nina on 2017/10/23.
 */
public interface IRealNameAuthService {
    Users getUserFromAddress(String Address);

    void insertPictureTemp(PictureTemp pictureTemp);

    void updatePictureTemp(PictureTemp pictureTemp);

    PictureTemp getPictureTemp(String UUID, Integer PTypeId);

    Map<String, Object> addCustomerPerson(CustomerDetailPerson cdp, PackBundle bundle, String facePhotoBase64) throws IOException;

    Map<String, Object> fillUpRealNameInfo(String idCardPhotoBase64Back, String address, String identityNumber, String userName, String validDateStart, String validDateEnd,
               String sex, String nation, String birthDay, String homeAddr, PackBundle bundle);

    /**
     * 增加用户的云签实名认证时没有保存的信息
     * @param cdp
     * @param UUID
     */
    void addOtherCustomerPerson(CustomerDetailPerson cdp);

    EntityDetail queryEntityDetailHasValidDateByMobile(String mobile, String mobilePrefix);

    Entity queryEntityIsEnabledByUUID(String UUID);

    void uploadInfoForManualReview(String appKey, String address, String name, String idCard, String passport, String sign,
                                   String country, String countryCode, String validDateStart, String validDateEnd, String sex,
                                   String nation, String birthDay, String homeAddr, MultipartFile fileCardFrontName, MultipartFile fileCardBackName, MultipartFile fileHandCardName, MultipartFile fileHomeAddrName);

	Map<String, Object> addCustomerPerson(CustomerDetailPerson cdp, Users users, PackBundle bundle, Map<Integer, String> pictures, int status);
}
