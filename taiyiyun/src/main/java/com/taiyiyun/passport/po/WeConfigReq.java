package com.taiyiyun.passport.po;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;

public class WeConfigReq {

    @JSONField(name="Url")
    @JsonProperty("Url")
    private String url;

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
}
