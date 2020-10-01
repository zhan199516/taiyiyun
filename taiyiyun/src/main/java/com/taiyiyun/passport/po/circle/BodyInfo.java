package com.taiyiyun.passport.po.circle;

import java.io.Serializable;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by okdos on 2017/7/4.
 */
public class BodyInfo<T> implements Serializable{
	
	private static final long serialVersionUID = 6150779566192304669L;
	
	public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public T getMessages() {
        return messages;
    }

    public void setMessages(T messages) {
        this.messages = messages;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Boolean getHasMore() {
        return hasMore;
    }

    public void setHasMore(Boolean hasMore) {
        this.hasMore = hasMore;
    }

    @JSONField(name="Status")
    private String status;
    @JSONField(name="Messages")
    private T messages;
    @JSONField(name="ErrorMsg")
    private String errorMsg;
    @JSONField(name="Tag")
    private String tag;
    @JSONField(name="HasMore")
    private Boolean hasMore;
}
