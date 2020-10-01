package com.taiyiyun.passport.service.transfer;

import com.taiyiyun.passport.init.SpringContext;
import com.taiyiyun.passport.transfer.ITransferAccounts;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by okdos on 2017/7/18.
 */
public class ThirdAppManager {
    private static ThirdAppManager ourInstance = new ThirdAppManager();

    public static ThirdAppManager getInstance() {
        return ourInstance;
    }

    private Map<String, ITransferAccounts> mapAccounts;

    private ThirdAppManager() {
        mapAccounts = SpringContext.getBeans(ITransferAccounts.class);
    }

    public ITransferAccounts getAccountById(String appId){

        for (ITransferAccounts val : mapAccounts.values()) {
            if (appId.equals(val.getAppId())) {
                return val;
            }
        }

        return null;
    }

}
