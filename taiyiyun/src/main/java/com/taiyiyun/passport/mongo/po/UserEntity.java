package com.taiyiyun.passport.mongo.po;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user", language = "zh-CN")
public class UserEntity {

	@Id
	private String id;

	private NameEntity name;

	private Integer age;

	private Date birthday;

	private String[] special;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public NameEntity getName() {
		return name;
	}

	public void setName(NameEntity name) {
		this.name = name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public String[] getSpecial() {
		return special;
	}

	public void setSpecial(String[] special) {
		this.special = special;
	}

}
