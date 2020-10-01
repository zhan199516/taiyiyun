package com.taiyiyun.passport.po.redpacket;

import java.math.BigDecimal;

public class MessageData implements java.io.Serializable {

	private static final long serialVersionUID = -1749922146346443809L;
	private BigDecimal amount;
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
}
