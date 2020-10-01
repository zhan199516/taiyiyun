package com.taiyiyun.passport.po.group;

/**
 * Created by nina on 2017/12/1.
 */
public class SetGroupUserInfoParam {
    private String groupId;
    private String nikeName;
    private Integer msgReceiveType;
    private Integer showNikeName;
    private Integer topTalk;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getNikeName() {
        return nikeName;
    }

    public void setNikeName(String nikeName) {
        this.nikeName = nikeName;
    }

    public Integer getMsgReceiveType() {
        return msgReceiveType;
    }

    public void setMsgReceiveType(Integer msgReceiveType) {
        this.msgReceiveType = msgReceiveType;
    }

    public Integer getShowNikeName() {
        return showNikeName;
    }

    public void setShowNikeName(Integer showNikeName) {
        this.showNikeName = showNikeName;
    }

    public Integer getTopTalk() {
        return topTalk;
    }

    public void setTopTalk(Integer topTalk) {
        this.topTalk = topTalk;
    }
}
