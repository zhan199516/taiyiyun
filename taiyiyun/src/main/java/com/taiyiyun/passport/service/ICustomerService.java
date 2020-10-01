package com.taiyiyun.passport.service;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.sqlserver.po.Users;

public interface ICustomerService {

	public boolean checkAllowSendVerifyCode(String mobile);

	public void saveVerifyCode(String mobile, String verifyCode)  throws Exception;

	public boolean verifyCode(String mobile, String code, boolean clear);

	public Map<String, Object> realNameAuth(PackBundle bundle, Users users, String idcardNo, String imgDataNoSign, String userName, String national) throws Exception ;

	public Map<String, Object> realNameAuth(PackBundle bundle, Users users, String appKey, String identityNumber, String userName, String validDateStart, String validDateEnd,
			String nation, String sex, String homeAddr, String birthDay, String facePhotoBase64, String sign, String national) throws Exception;

	public Map<String, Object> checkAuthStatus(PackBundle bundle, Users users);

	public Map<String, Object> saveArtificialAuthInfo(PackBundle bundle, Users users, String idCard, String passport, String trueName, MultipartFile file1, MultipartFile file2, MultipartFile file3, MultipartFile file4);

}
