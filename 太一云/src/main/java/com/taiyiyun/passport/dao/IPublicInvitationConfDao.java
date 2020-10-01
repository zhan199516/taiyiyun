package com.taiyiyun.passport.dao;

import com.taiyiyun.passport.po.PublicInvitationConf;

public interface IPublicInvitationConfDao {
	
	public java.util.List<PublicInvitationConf> listByInvitationId(String invitationId);
	

}
