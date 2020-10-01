package com.taiyiyun.passport.po.redpacket;

/***
 * 获取红包状态接口返回的数据包
 * 
 * @author wang85li
 *
 */
public class RedpacketStatus implements java.io.Serializable {

	private static final long serialVersionUID = 438404414832882561L;

	// 红包ID
	private String redpacketId;
	// //发红包用户id
	private String userId;
	// //红包类型，0：群组红包（随机和普通） 1：个人红包（普通）
	private Short redpacketType;

	/***
	 * 
	 * 红包状态： 0：可抢红包（调用抢红包接口） 1：已抢红包（调用红包明细查询接口） 2：未抢到红包（提示未抢到信息，点击“查看红包明细”，查看红包信息）
	 * 3：过期红包 （直接显示红包过期信息） 4：已失效（用户解绑，目前只考虑到此一种情况）
	 */
	private Integer redpacketStatus;
	// 发红包用户姓名
	private String userName;
	// 发红包用户头像
	private String avatarUrl;
	// 附加文字
	private String text;

	
	public String getRedpacketId() {
		return redpacketId;
	}

	public void setRedpacketId(String redpacketId) {
		this.redpacketId = redpacketId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Short getRedpacketType() {
		return redpacketType;
	}

	public void setRedpacketType(Short s) {
		this.redpacketType = s;
	}

	public Integer getRedpacketStatus() {
		return redpacketStatus;
	}

	public void setRedpacketStatus(Integer redpacketStatus) {
		this.redpacketStatus = redpacketStatus;
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

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
