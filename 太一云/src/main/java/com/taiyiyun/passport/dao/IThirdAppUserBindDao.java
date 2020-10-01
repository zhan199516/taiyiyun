package com.taiyiyun.passport.dao;

import com.taiyiyun.passport.po.ThirdApp;
import com.taiyiyun.passport.po.ThirdAppExt;
import com.taiyiyun.passport.po.ThirdAppUserBind;
import com.taiyiyun.passport.po.ThirdAppUserBindExt;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IThirdAppUserBindDao {
	List<ThirdAppUserBindExt> getExtByCondition(@Param("uuid") String uuid, @Param("appId") String appId, @Param("appKey") String appKey);

	ThirdAppUserBindExt getOneExtByCondition(@Param("uuid") String uuid, @Param("appId") String appId, @Param("appKey") String appKey);

	int updateBinding(ThirdAppUserBind thirdAppUserBind);

	int updateToken(ThirdAppUserBind thirdAppUserBind);

	int updateUnBinding(@Param("uuid") String uuid, @Param("appId") String appId);

	String getRelateId(String appId);
}
