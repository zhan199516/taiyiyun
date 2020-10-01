package com.taiyiyun.passport.po;

import java.io.Serializable;

public class PublicArticleRead implements Serializable {

	private static final long serialVersionUID = 4217000024522166883L;

	private String articleId;

	private Integer readCount;

	public String getArticleId() {
		return articleId;
	}

	public void setArticleId(String articleId) {
		this.articleId = articleId;
	}

	public Integer getReadCount() {
		return readCount;
	}

	public void setReadCount(Integer readCount) {
		this.readCount = readCount;
	}

}
