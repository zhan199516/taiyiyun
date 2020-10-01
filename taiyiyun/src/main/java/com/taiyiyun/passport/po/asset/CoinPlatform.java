package com.taiyiyun.passport.po.asset;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by okdos on 2017/7/7.
 * 可绑定平台信息
 */
public class CoinPlatform {
    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public List<Coin> getCoinList() {
        return coinList;
    }

    public void setCoinList(List<Coin> coinList) {
        this.coinList = coinList;
    }

    public Double getAvailableRmb() {
        return availableRmb;
    }

    public void setAvailableRmb(Double availableRmb) {
        this.availableRmb = availableRmb;
    }

    public String getPlatformLogo() {
        return platformLogo;
    }

    public void setPlatformLogo(String platformLogo) {
        this.platformLogo = platformLogo;
    }

    public String getBindUri() {
        return bindUri;
    }

    public void setBindUri(String bindUri) {
        this.bindUri = bindUri;
    }

    public Boolean getBinded() {
        return binded;
    }

    public void setBinded(Boolean binded) {
        this.binded = binded;
    }

    public String getPlatformCheck() {
        return platformCheck;
    }

    public void setPlatformCheck(String platformCheck) {
        this.platformCheck = platformCheck;
    }

    public String getThirdpartAppkey() {
        return thirdpartAppkey;
    }

    public void setThirdpartAppkey(String thirdpartAppkey) {
        if(thirdpartAppkey != null)
            this.thirdpartAppkey = thirdpartAppkey;
    }

    //平台id
    private String platformId;
    //平台名称
    private String platformName;
    //平台的logo
    private String platformLogo;
    //绑定的地址
    private String bindUri;

    //第三方平台appKey
    private String thirdpartAppkey = "";

    //是否已经绑定
    private Boolean binded = false;

    //可用转账余额
    private Double availableRmb;

    //第三方平台连接状态
    private String platformCheck;

    //平台币种
    private List<Coin> coinList = new ArrayList<>();

}
