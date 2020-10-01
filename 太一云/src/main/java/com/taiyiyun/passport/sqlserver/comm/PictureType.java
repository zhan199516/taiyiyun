package com.taiyiyun.passport.sqlserver.comm;

import java.util.Arrays;
import java.util.List;

import com.taiyiyun.passport.util.StringUtil;

/**
 * Created by nina on 2017/10/30.
 */
public enum PictureType {

    IDCARD_FRONT(1, "身份证正面", 1),
    IDCARD_BACK(2, "身份证反面", 1),
    IDCARD_HANDHOLD(3, "身份证手持", 1),
    BUSSINESS_LICENSE(4, "营业执照", 3),
    ORG_CODE_CERTIFICATE(5, "组织机构代码证", 3),
    TAX_REGISTRATION_CERTIFICATE(6, "税务登记证", 3),
    USER_PHOTO(7, "用户头像", 0),
    PASSPORT_PIC(8, "护照图片", 2),
    PASSPORT_HANDHOLD(9, "护照手持", 2),
    LEGAL_PERSON_FRONT(10, "法人证件正面", 3),
    LEGAL_PERSON_BACK(11, "法人证件反面", 3),
    ENTERPRISE_LOGO(12, "企业LOGO", 3),
    YUN_SIGN_PHOTO(13, "云签头像", 4),
    PROOF_OF_ADDRESS(14, "住址证明", 1),
    PASSPORT_BACK(15, "护照反面", 2);


    PictureType(int ptypeId, String ptypeName, int entityClass) {
        this.ptypeId = ptypeId;
        this.ptypeName = ptypeName;
        this.entityClass = entityClass;
    }
    private int ptypeId;
    private String ptypeName;
    private int entityClass;

    public int getPtypeId() {
        return ptypeId;
    }

    public void setPtypeId(int ptypeId) {
        this.ptypeId = ptypeId;
    }

    public String getPtypeName() {
        return ptypeName;
    }

    public void setPtypeName(String ptypeName) {
        this.ptypeName = ptypeName;
    }

    public int getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(int entityClass) {
        this.entityClass = entityClass;
    }
    
    public static PictureType get(String enumName) {
    	if (StringUtil.isEmpty(enumName)) {
    		return null;
    	}
    	
    	if ("IDCardFront".equals(enumName)) {
    		return IDCARD_FRONT;
    	} else if ("IDCardBack".equals(enumName)) {
    		return IDCARD_BACK;
    	} else if ("IDCardHandheld".equals(enumName)) {
    		return IDCARD_HANDHOLD;
    	} else if ("BusinessLicense".equals(enumName)) {
    		return BUSSINESS_LICENSE;
    	} else if ("OrganizationCode".equals(enumName)) {
    		return ORG_CODE_CERTIFICATE;
    	} else if ("RegistrationNumber".equals(enumName)) {
    		return TAX_REGISTRATION_CERTIFICATE;
    	} else if ("HeadPicture".equals(enumName)) {
    		return USER_PHOTO;
    	} else if ("PassportFront".equals(enumName)) {
    		return PASSPORT_PIC;
    	} else if ("PassportHandheld".equals(enumName)) {
    		return PASSPORT_HANDHOLD;
    	} else if ("LegalPersonIDCardFront".equals(enumName)) {
    		return LEGAL_PERSON_FRONT;
    	} else if ("LegalPersonIDCardBack".equals(enumName)) {
    		return LEGAL_PERSON_BACK;
    	} else if ("Logo".equals(enumName)) {
    		return ENTERPRISE_LOGO;
    	} else if ("YunSignHeadPicture".equals(enumName)) {
    		return YUN_SIGN_PHOTO;
    	} else if ("AddressPicture".equals(enumName)) {
    		return PROOF_OF_ADDRESS;
    	} else if ("PassportBack".equals(enumName)) {
    		return PASSPORT_BACK;
    	}
    	return null;
    }
    
    public static boolean checkExistType (String... enumNames) {
    	List<String> enumTypes = Arrays.asList("IDCardFront", "IDCardBack", "IDCardHandheld", "BusinessLicense", "OrganizationCode", 
    			"RegistrationNumber", "HeadPicture", "PassportFront", "PassportHandheld", "LegalPersonIDCardFront", 
    			"LegalPersonIDCardBack", "Logo", "YunSignHeadPicture", "AddressPicture", "PassportBack");
    	if (enumNames == null || enumNames.length <= 0) {
    		return false;
    	}
    	
    	int lg = enumNames.length;
    	for (int i = 0; i < lg; i++) {
    		String enumName = enumNames[i];
    		if (!enumTypes.contains(enumName)) {
    			return false;
    		}
    	}
    	return true;
    }
}
