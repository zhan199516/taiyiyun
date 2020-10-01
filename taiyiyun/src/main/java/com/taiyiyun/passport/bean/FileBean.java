package com.taiyiyun.passport.bean;

import java.io.File;
import java.io.Serializable;

public class FileBean implements Serializable {
	private static final long serialVersionUID = 8081261958439357681L;
	
	private String absolutePath;
	private File file;
	private String relativePath;
	private String rootDirectory;
	private String extName;

	public String getAbsolutePath() {
		return absolutePath;
	}

	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	public String getRootDirectory() {
		return rootDirectory;
	}

	public void setRootDirectory(String rootDirectory) {
		this.rootDirectory = rootDirectory;
	}

	public String getExtName() {
		return extName;
	}

	public void setExtName(String extName) {
		this.extName = extName;
	}

}
