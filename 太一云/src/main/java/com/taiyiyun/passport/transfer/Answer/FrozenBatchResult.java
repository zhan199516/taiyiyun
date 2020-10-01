package com.taiyiyun.passport.transfer.Answer;

import java.util.List;
import java.util.Map;

/**
 * Created by okdos on 2017/7/17.
 * 返回值：批量冻结返回值
 */
public class FrozenBatchResult extends ErrorCodeResult {

    List<Map<String,Object>> result;
    private String msg;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<Map<String,Object>> getResult() {
        return result;
    }

    public void setResult(List<Map<String,Object>> result) {
        this.result = result;
    }
}
