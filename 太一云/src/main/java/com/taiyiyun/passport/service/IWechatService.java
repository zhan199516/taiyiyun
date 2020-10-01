package com.taiyiyun.passport.service;

import com.taiyiyun.passport.po.WeiConfigRes;

public interface IWechatService {

    WeiConfigRes singature(String url);
}
