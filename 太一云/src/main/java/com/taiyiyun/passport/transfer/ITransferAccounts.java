package com.taiyiyun.passport.transfer;

import com.taiyiyun.passport.exception.DefinedError;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.po.ThirdAppUserBindExt;
import com.taiyiyun.passport.transfer.Answer.*;
import com.taiyiyun.passport.transfer.Answer.TransferResult;
import com.taiyiyun.passport.transfer.Ask.AskBody;
import com.taiyiyun.passport.transfer.Ask.AskParam;

import java.math.BigDecimal;
import java.util.List;

public interface ITransferAccounts {

	String getAppId();

	String buildBindUrl(ThirdAppUserBindExt ext);

	AskBody askForToken(PackBundle packBundle, ThirdAppUserBindExt ext, String relateId) throws DefinedError;
	AssetResult askForAsset(PackBundle packBundle, AskBody body) throws DefinedError ;
	AskFeeResult askForFee(PackBundle packBundle, AskBody body, String coinName, BigDecimal amount) throws DefinedError ;
	FrozenResult askForFroze(PackBundle packBundle, AskBody body, String coinName, BigDecimal fronum) throws DefinedError ;
	FrozenBatchResult askForBatchFroze(PackBundle packBundle, AskBody body, String coinName,BigDecimal totalAmount, List<AskParam> askParams) throws DefinedError ;
	FrozenResult askForUnfreeze(PackBundle packBundle, AskBody body, String coinName, BigDecimal fronum, Long froId) throws DefinedError ;
	TransferResult askForTransfer(PackBundle packBundle, AskBody body, String coinName, BigDecimal fronum, Long froId, BigDecimal fee, String toUserKey) throws DefinedError ;

	ErrorCodeResult askForUnbind(PackBundle packBundle, AskBody askBody) throws DefinedError ;

	List<CoinInfo> askForCoinInfos(String baseUrl);

	LimitResult askForLimit(PackBundle packBundle, AskBody body,BigDecimal amount) throws DefinedError ;
}
