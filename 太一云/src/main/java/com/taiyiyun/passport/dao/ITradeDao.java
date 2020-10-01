package com.taiyiyun.passport.dao;

import com.taiyiyun.passport.po.asset.Trade;
import com.taiyiyun.passport.po.asset.TransferAnswer;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by okdos on 2017/7/7.
 */
public interface ITradeDao {

    int saveTransApply(TransferAnswer answer);

    int insert(TransferAnswer answer);

    Trade getByTradeId(Long tradeId);

    List<Trade> listByToken(Trade trade);

    List<Trade> getHistory(@Param("userId") String userId,
                           @Param("ioType") Integer ioType,
                           @Param("start") Long start,
                           @Param("end") Long end,
                           @Param("platformId") String platformId,
                           @Param("coinId") String coinId,
                           @Param("line") Integer line);

    int acceptApply(@Param("tradeId") Long tradeId,
                      @Param("status") Integer status,
                      @Param("acceptTime") Long acceptTime,
                      @Param("error") String error,
                      @Param("fromOverBegin") BigDecimal fromOverBegin,
                      @Param("fromOverEnd") BigDecimal fromOverEnd,
                      @Param("toOverBegin") BigDecimal toOverBegin,
                      @Param("toOverEnd") BigDecimal toOverEnd,
                      @Param("worthRmbAccept") BigDecimal worthRmbAccept
                      );

    List<Trade> getTimeout(long aim);

    int updateUuid(@Param("tradeId") Long tradeId, @Param("toUuid") String toUuid);

    int updateFrozenId(Trade trade);
    
    Trade getTradeInfoByTradeId(Long tradeId);
    
    int saveTransferInfo(Trade trade);
}
