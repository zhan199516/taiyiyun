package com.taiyiyun.passport.service;

import com.taiyiyun.passport.commons.ResultData;
import com.taiyiyun.passport.exception.DefinedError;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.po.asset.Trade;
import com.taiyiyun.passport.po.asset.TransferAccept;
import com.taiyiyun.passport.po.asset.TransferAnswer;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;

/**
 * Created by okdos on 2017/7/7.
 */
public interface ITransferService {

    // 提出转账申请
    DeferredResult<String> postMoney(PackBundle packBundle, TransferAnswer apply);

    /**
     * 转账冻结
     * @param packBundle
     * @param apply
     * @return
     */
    String transferFrozen(PackBundle packBundle, TransferAnswer apply) throws DefinedError;

    // 确认转账
    DeferredResult<String> acceptMoney(PackBundle packBundle, TransferAccept accept);

    Trade getTradeByTradeNo(String tradeNo, String viewUuid, String viewUserId);

    List<Trade> getTradeHistory(PackBundle packBundle,Long start, Long end, Integer ioType, String platformId, String coinId, Integer line, String viewUserId, String viewUuid);

    /**
     * 用户首次绑定资产平台后，将入群奖励和红包资产转入到对应的资产平台下
     * @param packBundle
     * @param uuid
     * @return
     */
    ResultData<Integer> prizeTransferAccount(PackBundle packBundle, String uuid);
}
