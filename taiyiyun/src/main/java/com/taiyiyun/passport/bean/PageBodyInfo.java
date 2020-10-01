package com.taiyiyun.passport.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nina on 2017/12/13.
 */
public class PageBodyInfo<T> implements Serializable {

    private static final long serialVersionUID = 8237168765763425919L;

    private String tag;

    private List<T> list;

    private Boolean hasMore = false;

    private Integer pageSize = 10;

    private Integer start = 0;

    private List<T> dataList;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public Boolean getHasMore() {
        if(null == this.list || this.list.size() <= 0) {
            return false;
        }
        if((this.start + this.pageSize) < this.list.size()) {
            return true;
        }
        return hasMore;
    }

    public void setHasMore(Boolean hasMore) {
        this.hasMore = hasMore;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public List<T> getDataList() {
        if(this.start >= this.list.size()) {
            return new ArrayList<>();
        }
        if((this.start + this.pageSize) <= this.list.size()) {
            return this.list.subList(this.start, this.start + this.pageSize);
        }
        return this.list.subList(this.start, this.list.size());
    }

    public void setDataList(List<T> dataList) {
        this.dataList = dataList;
    }
}
