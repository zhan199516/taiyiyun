package com.taiyiyun.passport.bean;

import java.io.File;
import java.io.Serializable;

public class ImageBean implements Serializable {
	private static final long serialVersionUID = -4563297603715764317L;

	private String id;
	private File file;
	private String imageType;
	private String realPath;
	private String fileName;
	private String extName;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getImageType() {
		return imageType;
	}

	public void setImageType(String imageType) {
		this.imageType = imageType;
	}

	public String getRealPath() {
		return realPath;
	}

	public void setRealPath(String realPath) {
		this.realPath = realPath;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getExtName() {
		return extName;
	}

	public void setExtName(String extName) {
		this.extName = extName;
	}

}
