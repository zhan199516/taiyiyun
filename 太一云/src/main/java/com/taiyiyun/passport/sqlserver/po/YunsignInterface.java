package com.taiyiyun.passport.sqlserver.po;

import java.io.Serializable;
import java.util.Date;

/**
 * 描述：云签接口调用异常记录
 * 
 * @author chenzehui
 * @date 2018年3月7日
 */
public class YunsignInterface implements Serializable {

	private static final long serialVersionUID = 2841695448894960902L;

	private Long getId;

	private String uuid;

	private Integer interfaceType;

	private Integer status;

	private Date creationTime;

	private String code;

	private String rDesc;

	public Long getGetId() {
		return getId;
	}

	public String getUuid() {
		return uuid;
	}

	public Integer getInterfaceType() {
		return interfaceType;
	}

	public Integer getStatus() {
		return status;
	}

	public Date getCreationTime() {
		return creationTime;
	}

	public String getCode() {
		return code;
	}

	public String getrDesc() {
		return rDesc;
	}

	public void setGetId(Long getId) {
		this.getId = getId;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public void setInterfaceType(Integer interfaceType) {
		this.interfaceType = interfaceType;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setrDesc(String rDesc) {
		this.rDesc = rDesc;
	}

}
