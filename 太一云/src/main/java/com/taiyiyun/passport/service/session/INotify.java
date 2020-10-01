package com.taiyiyun.passport.service.session;

import com.taiyiyun.passport.bean.UserDetails;

public interface INotify {
    void callback(LoginInfo cache);
}
