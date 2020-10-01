package com.taiyiyun.passport.mqtt;

import java.io.Serializable;

public interface IContentTypeInterface extends Serializable {
	
	void setContentType(Integer contentType);
	
	Integer getContentType();
	
}
