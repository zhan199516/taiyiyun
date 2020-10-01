package com.taiyiyun.passport.po.circle;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class ArticleMessageContent {

	private static final long serialVersionUID = 408458988121464031L;

	@JSONField(name = "articleId")
	private String articleId;
	@JSONField(name = "title")
	private String title;
	@JSONField(name = "text")
	private String text;
	@JSONField(name = "attachFiles")
	private List<String> attachFiles;
	@JSONField(name = "attachFileUrls")
	private List<String> attachFileUrls;
	@JSONField(name = "isOriginal")
	private Boolean isOriginal;
	@JSONField(name = "articleHash")
	private String articleHash;

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

	public Boolean isOriginal() {
		return isOriginal;
	}

	public void setOriginal(Boolean isOriginal) {
		this.isOriginal = isOriginal;
	}

	public String getArticleHash() {
		return articleHash;
	}

	public void setArticleHash(String articleHash) {
		this.articleHash = articleHash;
	}

}
