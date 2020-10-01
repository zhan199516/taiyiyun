package com.taiyiyun.passport.po.group;

/**
 * Created by nina on 2017/11/14.
 */
public class GroupMemberSearchDto {


    /*群id*/
    private String groupId;
    /*查询内容*/
    private String content;
    /*分页时间搓*/
    private Long timestamp;
    /*每页记录数*/
    private Integer rows;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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
}
