package com.taiyiyun.passport.service;

import com.taiyiyun.passport.po.PublicInvitationDeliveryAddress;
import com.taiyiyun.passport.po.PublicInvitationUserRegister;

/***
 * 邀请相关业务
 * @author wang85li
 *
 */
public interface IPublicInvitationService {
	public boolean insert(PublicInvitationUserRegister publicInvitationUserRegister);
	public Integer getInvitationUserCount(String invitationId,String invitationUserId);
	public boolean insert(PublicInvitationDeliveryAddress publicInvitationDeliveryAddress);
	public java.util.Map<String, Object> getInvitationConf(String path);
	public boolean isPublicInvitationDeliveryAddress (String invitationId,String invitationUserId,String mobile);
	
}
