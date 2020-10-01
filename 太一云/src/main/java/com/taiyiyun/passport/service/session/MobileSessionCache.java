package com.taiyiyun.passport.service.session;

import com.taiyiyun.passport.bean.UserDetails;
import com.taiyiyun.passport.consts.Const;
import com.taiyiyun.passport.exception.DefinedError;
import com.taiyiyun.passport.init.SpringContext;
import com.taiyiyun.passport.service.IRedisService;
import com.taiyiyun.passport.util.IFunctionDelay;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 用户记录uuid
 */
public class MobileSessionCache {
    private static MobileSessionCache ourInstance = new MobileSessionCache();

    public static MobileSessionCache getInstance() {
        return ourInstance;
    }

    private MobileSessionCache() {
    }

    private Lock lock  = new ReentrantLock();

    private HashMap<String, HttpSession> sessionCache = new HashMap<>();


    private static UserDetails getUserDetails(HttpSession session){
        return (UserDetails)session.getAttribute(Const.SESSION_SHARE_NUM);
    }


    public static LoginInfo getLoginInfo(HttpSession session){
        LoginInfo loginInfo = (LoginInfo) session.getAttribute(Const.SESSION_LOGIN);

        return loginInfo;
    }

    private final String business = "mobile.cache.";

    private IRedisService redisService = SpringContext.getBean(IRedisService.class);

    private void saveCache(LoginInfo cache){
        redisService.put(business + cache.getMobile(), cache, 3600 * 24 * 7);
    }

    private LoginInfo getCache(String mobile){
        return (LoginInfo)redisService.get(business + mobile);
    }

    private LoginInfo createCache(HttpSession session){
        UserDetails userDetails = getUserDetails(session);
        if(userDetails == null){
            return null;
        }

        LoginInfo loginInfo = getLoginInfo(session);
        if(loginInfo == null){
            return null;
        }

        loginInfo.setUuid(userDetails.getUuid());
        loginInfo.setUserId(userDetails.getUserId());

        return loginInfo;
    }

    public void setCurrentSessionStatus(String mobile, int status){
        LoginInfo oldCache = getCache(mobile);
        if(oldCache == null){
            return;
        }

        oldCache.setStatus(status);
        saveCache(oldCache);
    }

    public LoginInfo getCurrentSession(String mobile){
        LoginInfo oldCache = getCache(mobile);
        return oldCache;
    }

    /**
     * 获取session的时候，存储此session（如果这个session还没有被记录，以便解决停机时候的问题）
     * @param session
     */
    public boolean isValidSession(HttpSession session, final boolean isLogin, final INotify notify){
        LoginInfo newCache = createCache(session);
        //UserDetails失效了或没有登录
        if(newCache == null){
            return false;
        }

        final LoginInfo oldCache = getCache(newCache.getMobile());

        //过期失效
        if( oldCache == null){
            saveCache(newCache);
            return true;
        }

        //如果使第一次登录，则需要挤掉其它用户，否则仅仅判断是否使缓存的session
        if(isLogin){

            boolean result;
            IFunctionDelay functionDelay = null;

            lock.lock();
            try{
                if(newCache.getSessionId().equals(oldCache.getSessionId())){
                    saveCache(newCache);
                    result = true;
                } else
                    //挤掉其它用户
                if(newCache.getLoginTime() >= oldCache.getLoginTime()){
                    saveCache(newCache);

                    //todo 通知旧设备被踢
                    if(notify != null){
                        functionDelay = new IFunctionDelay() {
                            @Override
                            public void run() {
                                notify.callback(oldCache);
                            }

                            @Override
                            public <T> T execute() {
                                return null;
                            }
                        };
                    }

                    result = true;
                } else {
                    result = false;
                }
            } finally {
                lock.unlock();
            }

            if(functionDelay != null){
                functionDelay.run();
            }

            return result;

        } else {
            if(oldCache.getStatus() != DefinedError.Status.SUCC.getValue()){
                if(notify != null) {
                    notify.callback(oldCache);
                }
                return false;
            }

            if(oldCache.getSessionId().equals(newCache.getSessionId())){
                return true;
            }
            else
            {
                if(notify != null) {
                    notify.callback(oldCache);
                }
                return false;
            }
        }


    }
}
