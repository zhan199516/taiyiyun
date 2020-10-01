package com.taiyiyun.passport.transfer.Answer;

import java.util.List;

/**
 * Created by okdos on 2017/7/17.
 * 获取资产信息的返回值
 */
public class AssetResult extends ErrorCodeResult {

    public Double getSurplus() {
        return surplus;
    }

    public void setSurplus(Double surplus) {
        this.surplus = surplus;
    }

    public List<CoinOwn> getCoinList() {
        return coinList;
    }

    public void setCoinList(List<CoinOwn> coinList) {
        this.coinList = coinList;
    }

    private Double surplus;
    private List<CoinOwn> coinList;
}
