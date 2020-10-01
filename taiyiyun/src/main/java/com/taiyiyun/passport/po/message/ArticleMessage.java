package com.taiyiyun.passport.po.message;

import java.util.List;

/**
 * Created by okdos on 2017/7/10.
 */
public class ArticleMessage {
    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getAttachFiles() {
        return attachFiles;
    }

    public void setAttachFiles(List<String> attachFiles) {
        this.attachFiles = attachFiles;
    }

    public List<String> getAttachFileUrls() {
        return attachFileUrls;
    }

    public void setAttachFileUrls(List<String> attachFileUrls) {
        this.attachFileUrls = attachFileUrls;
    }

    public boolean isOriginal() {
        return isOriginal;
    }

    public void setOriginal(boolean original) {
        isOriginal = original;
    }

    public String getArticleHash() {
        return articleHash;
    }

    public void setArticleHash(String articleHash) {
        this.articleHash = articleHash;
    }


    private String articleId;
    private String title;
    private String text;
    private List<String> attachFiles;
    private List<String> attachFileUrls;
    private boolean isOriginal;
    private String articleHash;
}
