package com.taiyiyun.passport.service.queue;

import com.taiyiyun.passport.exception.DefinedError;

/**
 * Created by okdos on 2017/7/8.
 */
public interface ITask {
    void run() throws DefinedError;
}
