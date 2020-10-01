package com.taiyiyun.passport.sqlserver.comm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.taiyiyun.passport.sqlserver.po.Entity;
import com.taiyiyun.passport.sqlserver.po.UserEntity;
import com.taiyiyun.passport.sqlserver.po.Users;
import com.taiyiyun.passport.util.StringUtil;

public class UsersInfoBody implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<UserEntity> userEntitys;

	private Users users;

	private List<Entity> entitys;
	
	private String privKey;
	
	public UsersInfoBody() {
		
	}

	public UsersInfoBody(List<UserEntity> userEntitys, Users users, List<Entity> entitys) {
		this.userEntitys = userEntitys;
		this.users = users;
		this.entitys = entitys;
	}

	public List<UserEntity> getUserEntitys() {
		return userEntitys;
	}

	public Users getUsers() {
		return users;
	}

	public List<Entity> getEntitys() {
		return entitys;
	}

	public void setUserEntitys(List<UserEntity> userEntitys) {
		this.userEntitys = userEntitys;
	}

	public void setUsers(Users users) {
		this.users = users;
	}

	public void setEntitys(List<Entity> entitys) {
		this.entitys = entitys;
	}
	
	public String getPrivKey() {
		return privKey;
	}

	public void setPrivKey(String privKey) {
		this.privKey = privKey;
	}

	public Map<String, Object> getBody() {
		if (StringUtil.isEmpty(privKey) || userEntitys == null || entitys == null || users == null) {
			throw new IllegalArgumentException("privKey or userEntitys or entitys or users is null");
		}
		
		Map<String, Object> rsMap = new HashMap<String, Object>();
		Map<String, Object> outputMap = new HashMap<String, Object>();
		Map<String, Object> myPkSkMap = new HashMap<String, Object>();
		
		outputMap.put("CreationTime", users.getCreationTime());
		outputMap.put("Address", users.getAddress());
		outputMap.put("Mobile", users.getMobile());
		outputMap.put("Status", users.getStatus());
		outputMap.put("NikeName", users.getNikeName());
		outputMap.put("HeadPicture", users.getHeadPicture());
		outputMap.put("UUID", users.getUUID());
		
		int size = this.userEntitys.size();
		for (int i = 0; i < size; i++) {
			UserEntity userEntity = this.userEntitys.get(i);
			int lg = entitys.size();
			for (int n = 0; n < lg; n++) {
				Entity entity = entitys.get(n);
				if (StringUtil.isNotEmpty(entity.getEntityId()) && entity.getEntityId().equals(userEntity.getEntityId())) {
					Map<String, Object> entityMap = new HashMap<String, Object>();
					entityMap.put("Type", entity.getTypeId());
					entityMap.put("UserEntityId", userEntity.getUserEntityId());
					entityMap.put("EntityStatus", entity.getStatus());
					entityMap.put("GradeId", entity.getGradeId());
					outputMap.put("Entitys", entityMap);
				}
			}
		}
		
		myPkSkMap.put("Address", users.getAddress());
		myPkSkMap.put("PrivKey", getPrivKey());
		
		rsMap.put("myOutPutUser", outputMap);
		rsMap.put("myPkSk", myPkSkMap);
		
		return rsMap;
	}
	
	public Map<String, Object> getOutPutUser() {
		if (userEntitys == null || entitys == null || users == null) {
			throw new IllegalArgumentException("userEntitys or entitys or users is null");
		}
		
		Map<String, Object> outputMap = new HashMap<String, Object>();
		
		outputMap.put("CreationTime", users.getCreationTime());
		outputMap.put("Address", users.getAddress());
		outputMap.put("Mobile", users.getMobile());
		outputMap.put("Status", users.getStatus());
		outputMap.put("NikeName", users.getNikeName());
		outputMap.put("HeadPicture", users.getHeadPicture());
		outputMap.put("UUID", users.getUUID());
		
		int size = this.userEntitys.size();
		for (int i = 0; i < size; i++) {
			UserEntity userEntity = this.userEntitys.get(i);
			int lg = entitys.size();
			for (int n = 0; n < lg; n++) {
				Entity entity = entitys.get(n);
				if (StringUtil.isNotEmpty(entity.getEntityId()) && entity.getEntityId().equals(userEntity.getEntityId())) {
					Map<String, Object> entityMap = new HashMap<String, Object>();
					entityMap.put("Type", entity.getTypeId());
					entityMap.put("UserEntityId", userEntity.getUserEntityId());
					entityMap.put("EntityStatus", entity.getStatus());
					entityMap.put("GradeId", entity.getGradeId());
					outputMap.put("Entitys", entityMap);
				}
			}
		}
		
		return outputMap;
	}

}
