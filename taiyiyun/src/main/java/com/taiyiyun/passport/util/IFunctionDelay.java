package com.taiyiyun.passport.util;

//代替lambda表达式，延迟执行某个数据
public interface IFunctionDelay {

    void run();

    <T> T execute();
}
