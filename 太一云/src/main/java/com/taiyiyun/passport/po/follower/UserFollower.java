package com.taiyiyun.passport.po.follower;

import java.io.Serializable;
import java.sql.Timestamp;

public class UserFollower implements Serializable {

	private static final long serialVersionUID = -1175035620811415244L;
	private String userId;
	private String followerId;
	private Timestamp focusTime;
	private String userName;
	private String description;
	private Long timestamp;
	private String avatarUrl;
	/*当前页数*/
	private Integer rows;
	/*每页记录数*/
	private Integer page;

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

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public Integer getRows() {
		return rows;
	}

	public void setRows(Integer rows) {
		this.rows = rows;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
