package com.taiyiyun.passport.po;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class PublicUserFollower implements Serializable {

	private static final long serialVersionUID = -1175035620811415244L;

	private Long id;
	private String userId;
	private String [] userIds;
	private String followerId;
	private List<PublicUser> followers;
	private Timestamp focusTime;

	private String userName;
	private String avatarUrl;
	private String thumbAvatarUrl;
	private String backgroundImgUrl;
	private String description;
	private Integer typeId;
	private Date createTime;
	private Long focusTimes;
	private Integer fansCount;
	private Integer followersCount;

	/*当前页数*/
	private Integer start;
	/*每页记录数*/
	private Integer offset;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getFollowerId() {
		return followerId;
	}

	public void setFollowerId(String followerId) {
		this.followerId = followerId;
	}

	public List<PublicUser> getFollowers() {
		return followers;
	}

	public void setFollowers(List<PublicUser> followers) {
		this.followers = followers;
	}

	public Timestamp getFocusTime() {
		return focusTime;
	}

	public void setFocusTime(Timestamp focusTime) {
		this.focusTime = focusTime;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public Long getFocusTimes() {
		return focusTimes;
	}

	public void setFocusTimes(Long focusTimes) {
		this.focusTimes = focusTimes;
	}

	public Integer getStart() {
		return start;
	}

	public void setStart(Integer start) {
		this.start = start;
	}

	public Integer getOffset() {
		return offset;
	}

	public void setOffset(Integer offset) {
		this.offset = offset;
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

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String[] getUserIds() {
		return userIds;
	}

	public void setUserIds(String[] userIds) {
		this.userIds = userIds;
	}

	public Integer getFollowersCount() {
		return followersCount;
	}

	public void setFollowersCount(Integer followersCount) {
		this.followersCount = followersCount;
	}

	public Integer getFansCount() {
		return fansCount;
	}

	public void setFansCount(Integer fansCount) {
		this.fansCount = fansCount;
	}
}
