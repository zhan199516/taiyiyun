package com.taiyiyun.passport.dao;

import com.taiyiyun.passport.po.LoginApp;

public interface ILoginAppDao {
    LoginApp getItem(String appKey);
}
