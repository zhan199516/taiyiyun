package com.taiyiyun.passport.mqtt.v2;

import com.taiyiyun.passport.init.SpringContext;

/**
 * Created by nina on 2018/3/16.
 */
public class DownLinkForwardJobHandler {

    private IMessageForwardService forwardService;

    public DownLinkForwardJobHandler() {
        forwardService = SpringContext.getBean(MessageForwardServiceImpl.class);
    }

    public void handleDownLinkForwardJob(DownLinkForwardJob downLinkForwardJob) {
        forwardService.receiveMessage(downLinkForwardJob.getTopic(), downLinkForwardJob.getMessage(), downLinkForwardJob.getMqMessageId(), downLinkForwardJob.getMqQos());
    }
}
