package com.taiyiyun.passport.service;

import com.taiyiyun.passport.po.mail.MailDto;

public interface IMailService {

    /**
     *  发送邮件
     * @param mail
     */
    public void sendEmail(MailDto mail);
}
