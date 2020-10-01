package com.taiyiyun.passport.service.impl;

import com.taiyiyun.passport.dao.IChatPictureDao;
import com.taiyiyun.passport.po.chat.ChatPicture;
import com.taiyiyun.passport.service.ChatPictureService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ChatPictureServiceImpl implements ChatPictureService {

	@Resource
	private IChatPictureDao dao;

	@Override
	public ChatPicture getByMd5(String picMd5) {
		return dao.getByMd5(picMd5);
	}

	@Override
	public void save(ChatPicture chatPicture) {
		dao.save(chatPicture);
		
	}

}
