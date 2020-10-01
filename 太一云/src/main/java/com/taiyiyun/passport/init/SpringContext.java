package com.taiyiyun.passport.init;

import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.mqtt.v2.UplinkMessageSubscriber;
import com.taiyiyun.passport.service.transfer.*;
import com.taiyiyun.passport.service.weichat.WechatToken;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SpringContext implements ApplicationContextAware {
	
	private static ApplicationContext context = null;

	@Override
	public void setApplicationContext(ApplicationContext cxt) throws BeansException {
		setContext(cxt);
		try {
			if(Config.get("mqtt.listen", true)){
				System.out.println("mqtt.listen=" + Config.get("mqtt.listen") + " " + Config.get("mqtt.listen", true));
//				MessageSubscriber.getInstance().subscribe();
				UplinkMessageSubscriber.getInstance().subscribe();
			}
			FrozenClear.getInstance().start();
			WechatToken.getInstance().start();
			AssetCache.getInstance().start();
			RedpacketClear.getInstance().start();
			//先注释掉转账通知和发红包限额处理
			RechargeNotice.getInstance().start();
			LimitAmountManager.getInstance().start();
			RedpacketBackPay.getInstance().start();
			RedpacketRepeatPay.getInstance().start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static final <T> T getBean(Class<T> clazz) {
		return context.getBean(clazz);
	}
	
	public static final void setContext(ApplicationContext cxt) {
		context = cxt;
	}
	
	public static final <T> Map<String, T> getBeans(Class<T> clazz) {
		return context.getBeansOfType(clazz, true, true);
	}
	
}
