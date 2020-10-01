package com.taiyiyun.passport.mqtt.v2;

import com.taiyiyun.passport.init.SpringContext;

/**
 * Created by nina on 2018/3/16.
 */
public class DownLinkLogJobHandler {

    private IMessageLogService logService;

    public DownLinkLogJobHandler() {
        logService = SpringContext.getBean(IMessageLogService.class);
    }

    public void handleDownLinkLogJob(DownLinkLogJob downLinkLogJob) {
        logService.receiveMessage(downLinkLogJob.getTopic(), downLinkLogJob.getMessage(), downLinkLogJob.getMqMessageId(), downLinkLogJob.getMqQos());
    }

}
