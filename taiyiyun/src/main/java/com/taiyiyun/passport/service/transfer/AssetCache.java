package com.taiyiyun.passport.service.transfer;

import com.taiyiyun.passport.dao.IThirdAppDao;
import com.taiyiyun.passport.init.SpringContext;
import com.taiyiyun.passport.po.ThirdAppExt;
import com.taiyiyun.passport.transfer.Answer.CoinInfo;
import com.taiyiyun.passport.transfer.ITransferAccounts;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by okdos on 2017/7/18.
 */
public class AssetCache {
    private static AssetCache ourInstance = new AssetCache();

    public static AssetCache getInstance() {
        return ourInstance;
    }
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private AssetCache() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        thirdAppDao = SpringContext.getBean(IThirdAppDao.class);
    }

    private ScheduledExecutorService executorService; // 定时任务
    private IThirdAppDao thirdAppDao;

    private Hashtable<String, CoinInfo> coinCache = new Hashtable<>();
    private void storeCoin(String platformId, CoinInfo coin){
//        logger.info(platformId + "." + coin.getName() + "--:" + coin);
        coinCache.put(platformId + "." + coin.getName(), coin);
    }
    public CoinInfo getCoin(String platformId, String coinId){
        return coinCache.get(platformId + "." + coinId);
    }


    public void start(){
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try{
                    refresh();
                } catch(Exception ex){
                    logger.error(ex.getMessage());
                }

            }
        }, 1, 60, TimeUnit.SECONDS);
    }

    public void stop(){
    }

    public void refresh(){

        List<ThirdAppExt> list = thirdAppDao.getAll();

        for(int i = 0; i < list.size(); i++){
            ThirdAppExt app = list.get(i);
            refreshOne(app);
        }
    }

    private void refreshOne(ThirdAppExt app){

        ITransferAccounts accounts = ThirdAppManager.getInstance().getAccountById(app.getAppId());

        if(accounts == null){
            return;
        }

        List<CoinInfo> list = accounts.askForCoinInfos(app.getCoinCallUrl());

        if(list == null){
            return;
        }

        for(CoinInfo info : list){
            String logo = info.getLogo();
            if(!StringUtils.contains(logo, "http")) {
                info.setLogo(app.getCoinCallUrl() + logo);
            }
            this.storeCoin(app.getAppId(), info);
        }
    }
}
