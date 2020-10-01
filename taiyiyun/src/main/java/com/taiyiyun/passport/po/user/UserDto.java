package com.taiyiyun.passport.po.user;

import java.io.Serializable;

public class UserDto implements Serializable {

	private static final long serialVersionUID = -2210181436770810929L;
	private String groupId;
	private String currentUserId;
	private String groupIds[];
	private String userIds[];
	private Integer needMemberNumber;
	private Integer needFansNumber;
	private Integer needFollowersNumber;

	public UserDto() {

	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String[] getGroupIds() {
		return groupIds;
	}

	public void setGroupIds(String[] groupIds) {
		this.groupIds = groupIds;
	}

	public String[] getUserIds() {
		return userIds;
	}

	public void setUserIds(String[] userIds) {
		this.userIds = userIds;
	}

	public Integer getNeedMemberNumber() {
		return needMemberNumber;
	}

	public void setNeedMemberNumber(Integer needMemberNumber) {
		this.needMemberNumber = needMemberNumber;
	}

	public Integer getNeedFansNumber() {
		return needFansNumber;
	}

	public void setNeedFansNumber(Integer needFansNumber) {
		this.needFansNumber = needFansNumber;
	}

	public Integer getNeedFollowersNumber() {
		return needFollowersNumber;
	}

	public void setNeedFollowersNumber(Integer needFollowersNumber) {
		this.needFollowersNumber = needFollowersNumber;
	}

	public String getCurrentUserId() {
		return currentUserId;
	}

	public void setCurrentUserId(String currentUserId) {
		this.currentUserId = currentUserId;
	}
}
