package com.taiyiyun.passport.po.redpacket;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

public class RedpacketReceive implements Serializable {

	private static final long serialVersionUID = -2210181436770810929L;

	@JSONField(name="userId")
	private String id;
	private String userName; // 公众号名称
	private String status; // 状态，0：待上线，1：已经上线，2：已经下线
	private String avatarUrl;// 公众号图标
	private Timestamp createTime;
	private String description;
	private Integer typeId;
	private String mobile;
	private String uuid;
	private Integer version;
	private String appKey;
	private String userKey;
	private String thumbAvatarUrl;
	private String backgroundImgUrl;
	private Boolean isBarrier;
	private Date useTime;
	private Long lastMsgPullTime; //最后的记录拉取时间
	private String mobilePrefix;


	public RedpacketReceive() {

	}

	public Long getLastMsgPullTime() {
		return lastMsgPullTime;
	}

	public void setLastMsgPullTime(Long lastMsgPullTime) {
		this.lastMsgPullTime = lastMsgPullTime;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getTypeId() {
		return typeId;
	}

	public void setTypeId(Integer typeId) {
		this.typeId = typeId;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	public String getUserKey() {
		return userKey;
	}

	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}

	public String getThumbAvatarUrl() {
		return thumbAvatarUrl;
	}

	public void setThumbAvatarUrl(String thumbAvatarUrl) {
		this.thumbAvatarUrl = thumbAvatarUrl;
	}

	public String getBackgroundImgUrl() {
		return backgroundImgUrl;
	}

	public void setBackgroundImgUrl(String backgroundImgUrl) {
		this.backgroundImgUrl = backgroundImgUrl;
	}

	public Date getUseTime() {
		return useTime;
	}

	public void setUseTime(Date useTime) {
		this.useTime = useTime;
	}

	public Boolean getIsBarrier() {
		return isBarrier;
	}

	public void setIsBarrier(Boolean isBarrier) {
		this.isBarrier = isBarrier;
	}

	public String getMobilePrefix() {
		return mobilePrefix;
	}

	public void setMobilePrefix(String mobilePrefix) {
		this.mobilePrefix = mobilePrefix;
	}
}
