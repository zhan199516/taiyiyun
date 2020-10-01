package com.taiyiyun.passport.po.redpacket;

import com.alibaba.fastjson.JSONObject;
import com.taiyiyun.passport.mqtt.Message;

public class RedPacketRetParam implements java.io.Serializable {

	private static final long serialVersionUID = 3975371781470878314L;
	private Integer redpacketStatus;
	private Message<JSONObject> message;

	
	public Integer getRedpacketStatus() {
		return redpacketStatus;
	}
	public void setRedpacketStatus(Integer redpacketStatus) {
		this.redpacketStatus = redpacketStatus;
	}
	public Message<JSONObject> getMessage() {
		return message;
	}
	public void setMessage(Message<JSONObject> message) {
		this.message = message;
	}
	
}
