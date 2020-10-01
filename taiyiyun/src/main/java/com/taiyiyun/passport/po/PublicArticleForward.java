package com.taiyiyun.passport.po;

import java.io.Serializable;
import java.util.Date;

public class PublicArticleForward implements Serializable {

	private static final long serialVersionUID = 3338202897554022879L;

	private Long forwardId;

	private String operatorId;

	private String articleId;

	private Integer toType;

	private Integer fromType;

	private Date createTime;

	public Long getForwardId() {
		return forwardId;
	}

	public void setForwardId(Long forwardId) {
		this.forwardId = forwardId;
	}

	public String getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}

	public String getArticleId() {
		return articleId;
	}

	public void setArticleId(String articleId) {
		this.articleId = articleId;
	}

	public Integer getToType() {
		return toType;
	}

	public void setToType(Integer toType) {
		this.toType = toType;
	}

	public Integer getFromType() {
		return fromType;
	}

	public void setFromType(Integer fromType) {
		this.fromType = fromType;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

}
