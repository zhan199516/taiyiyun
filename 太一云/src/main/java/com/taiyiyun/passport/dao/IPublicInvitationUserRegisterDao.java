package com.taiyiyun.passport.dao;

import com.taiyiyun.passport.po.PublicInvitationUserRegister;

public interface IPublicInvitationUserRegisterDao {

	public int insert(PublicInvitationUserRegister publicInvitationUserRegister);
		
	public java.util.List<PublicInvitationUserRegister> listByUserId(PublicInvitationUserRegister publicInvitationUserRegister);
	
	public java.util.List<PublicInvitationUserRegister> listByInvitationId(PublicInvitationUserRegister publicInvitationUserRegister);
}
