package com.taiyiyun.passport.po.circle;

import java.io.Serializable;
import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by okdos on 2017/6/28.
 */
public class ArticleContent implements Serializable{

	private static final long serialVersionUID = 7837223216752239788L;

	private String articleId;

	@JSONField(name = "Title")
	private String title;

	@JSONField(name = "Text")
	private String text;

	@JSONField(name = "Url")
	private String url;

	@JSONField(name = "Image")
	private String image;

	@JSONField(name = "ImageType")
	private String imageType;

	@JSONField(name = "ImageUrl")
	private String imageUrl;
	
	@JSONField(name = "Type")
	private Integer type;
	
	@JSONField(name = "ImageUrls")
	private List<String> imageUrls;

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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getImageType() {
		return imageType;
	}

	public void setImageType(String imageType) {
		this.imageType = imageType;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public List<String> getImageUrls() {
		return imageUrls;
	}

	public void setImageUrls(List<String> imageUrls) {
		this.imageUrls = imageUrls;
	}

}
