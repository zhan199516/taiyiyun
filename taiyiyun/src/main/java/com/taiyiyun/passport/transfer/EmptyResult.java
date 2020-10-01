package com.taiyiyun.passport.transfer;

import java.io.Serializable;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 空执行结果
 * @author LiuQing
 */
public class EmptyResult implements Serializable {

	@JSONField(serialize = false)
	private static final long serialVersionUID = 1603986459580397367L;

}
