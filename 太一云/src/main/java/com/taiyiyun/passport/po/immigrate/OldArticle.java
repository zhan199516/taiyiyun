package com.taiyiyun.passport.po.immigrate;

/**
 * Created by okdos on 2017/7/29.
 */
public class OldArticle {
    private String articleId;
    private String imageType;
    private byte[] image;
    private String imagePath;
    

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
}
