package com.taiyiyun.passport.po.redpacket;

/**
 * 抢到红包后通知发红包者消息体
 * @author wang85li
 *
 */
public class AcceptRedpacketMessage implements java.io.Serializable {

	private static final long serialVersionUID = -5575361002367899143L;
	
	private String redPacketId;
	private String fromUserId;
	private String fromUserName;
	private String toUserId;
	private String toUserName;
	private Long acceptTime;

	public String getToUserId() {
		return toUserId;
	}
	public void setToUserId(String toUserId) {
		this.toUserId = toUserId;
	}
	public String getToUserName() {
		return toUserName;
	}
	public void setToUserName(String toUserName) {
		this.toUserName = toUserName;
	}
	
	public String getRedPacketId() {
		return redPacketId;
	}
	public void setRedPacketId(String redPacketId) {
		this.redPacketId = redPacketId;
	}
	public String getFromUserId() {
		return fromUserId;
	}
	public void setFromUserId(String fromUserId) {
		this.fromUserId = fromUserId;
	}
	public String getFromUserName() {
		return fromUserName;
	}
	public void setFromUserName(String fromUserName) {
		this.fromUserName = fromUserName;
	}
	public Long getAcceptTime() {
		return acceptTime;
	}
	public void setAcceptTime(Long acceptTime) {
		this.acceptTime = acceptTime;
	}

}
