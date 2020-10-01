package com.taiyiyun.passport.mqtt;

import com.alibaba.fastjson.annotation.JSONField;

public class MessageContentType implements IContentTypeInterface {

	@JSONField(serialize = false)
	private static final long serialVersionUID = -4778329596313887725L;
	
	@JSONField(name = "ContentType")
	private Integer contentType;
	
	@Override
	public void setContentType(Integer contentType) {
		this.contentType = contentType;
	}

	@Override
	public Integer getContentType() {
		return contentType;
	}

}
