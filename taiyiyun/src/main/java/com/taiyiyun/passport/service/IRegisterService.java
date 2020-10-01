package com.taiyiyun.passport.service;

import com.taiyiyun.passport.language.PackBundle;

import java.util.Map;

public interface IRegisterService {

	public String saveRegisterInfo(PackBundle packBundle, Map<String, String> params);

}
