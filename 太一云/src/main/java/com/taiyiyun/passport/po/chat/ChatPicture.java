package com.taiyiyun.passport.po.chat;

import java.io.Serializable;
import java.util.Date;

public class ChatPicture implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5292404637393261928L;

	
	private Integer id;
	
	private String picMd5;

	private String picName;
	
	private String picHeight;
	
	private String picWidth;
	
	private Date createTime;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}



	public String getPicMd5() {
		return picMd5;
	}

	public void setPicMd5(String picMd5) {
		this.picMd5 = picMd5;
	}

	public String getPicHeight() {
		return picHeight;
	}

	public void setPicHeight(String picHeight) {
		this.picHeight = picHeight;
	}

	public String getPicWidth() {
		return picWidth;
	}

	public void setPicWidth(String picWidth) {
		this.picWidth = picWidth;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getPicName() {
		return picName;
	}

	public void setPicName(String picName) {
		this.picName = picName;
	}
}
