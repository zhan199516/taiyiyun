package com.taiyiyun.passport.service;

import com.taiyiyun.passport.po.chat.ChatPicture;

public interface ChatPictureService {

	public ChatPicture getByMd5(String picMd5);
	
	public void save(ChatPicture chatPicture);

}
