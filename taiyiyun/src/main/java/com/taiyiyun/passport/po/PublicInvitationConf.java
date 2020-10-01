package com.taiyiyun.passport.po;

/**
 * 邀请活动配置
 * @author wang85li
 *
 */
public class PublicInvitationConf implements java.io.Serializable {

	private static final long serialVersionUID = -7509287516821713479L;
	/** 邀请ID */
	private String invitationId;
	/** 创建邀请记录用户ID */
	private String createUserId;
	/** 邀请名称 */
	private String invitationName;
	private java.util.Date createTime;
	private java.util.Date upateTime;
	/** 过期时间 */
	private java.util.Date expiredTime;
	/** 0 正常 1 禁用 **/
	private Integer disabled;
	/** 图片地址 */
	private String imgUrl;
	/** H5页面地址 */
	private String pageUrl;
	private String sharePageUrl;
	/**描述*/
	private String description;
	/**缩略图URL*/
	private String thumbnailUrl;
	
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getThumbnailUrl() {
		return thumbnailUrl;
	}
	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	public String getSharePageUrl() {
		return sharePageUrl;
	}
	public void setSharePageUrl(String sharePageUrl) {
		this.sharePageUrl = sharePageUrl;
	}

	public String getImgUrl() {
		return imgUrl;
	}
	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}
	public String getPageUrl() {
		return pageUrl;
	}
	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}
	
	public String getInvitationId() {
		return invitationId;
	}
	public void setInvitationId(String invitationId) {
		this.invitationId = invitationId;
	}
	public String getCreateUserId() {
		return createUserId;
	}
	public void setCreateUserId(String createUserId) {
		this.createUserId = createUserId;
	}
	public String getInvitationName() {
		return invitationName;
	}
	public void setInvitationName(String invitationName) {
		this.invitationName = invitationName;
	}
	public java.util.Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(java.util.Date createTime) {
		this.createTime = createTime;
	}
	public java.util.Date getUpateTime() {
		return upateTime;
	}
	public void setUpateTime(java.util.Date upateTime) {
		this.upateTime = upateTime;
	}
	public java.util.Date getExpiredTime() {
		return expiredTime;
	}
	public void setExpiredTime(java.util.Date expiredTime) {
		this.expiredTime = expiredTime;
	}
	public Integer getDisabled() {
		return disabled;
	}
	public void setDisabled(Integer disabled) {
		this.disabled = disabled;
	}
	
}
