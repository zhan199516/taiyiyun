package com.taiyiyun.passport.dao;

import com.taiyiyun.passport.po.PublicInvitationDeliveryAddress;
import com.taiyiyun.passport.po.PublicInvitationUserRegister;

public interface IPublicInvitationDeliveryAddressDao {

	public int insert(PublicInvitationDeliveryAddress publicInvitationDeliveryAddress);
	public java.util.List<PublicInvitationDeliveryAddress> listByUserId(PublicInvitationDeliveryAddress publicInvitationDeliveryAddress);
	
}
