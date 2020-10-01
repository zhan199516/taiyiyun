package com.taiyiyun.passport.dao;

import com.taiyiyun.passport.po.chat.ChatPicture;


public interface IChatPictureDao {

	
	public ChatPicture getByMd5(String picMd5);
	
	public void save(ChatPicture chatPicture);

}
