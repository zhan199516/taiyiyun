package com.taiyiyun.passport.controller.asset;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.taiyiyun.passport.bean.UserDetails;
import com.taiyiyun.passport.controller.BaseController;
import com.taiyiyun.passport.exception.DefinedError;
import com.taiyiyun.passport.language.LangResource;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.po.PublicUser;
import com.taiyiyun.passport.po.ThirdAppUserBindExt;
import com.taiyiyun.passport.service.IPublicUserFollowerService;
import com.taiyiyun.passport.service.IPublicUserService;
import com.taiyiyun.passport.service.IThirdAppUserBindService;
import com.taiyiyun.passport.service.ITransferService;
import com.taiyiyun.passport.transfer.po.BindEntity;
import com.taiyiyun.passport.transfer.po.CallbackStatus;
import com.taiyiyun.passport.util.SessionUtil;
import com.taiyiyun.passport.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by okdos on 2017/7/14.
 */

@Controller
@RequestMapping(value="/api/money")
public class ApiBindThird extends BaseController{

    @Resource
    IThirdAppUserBindService userBindService;

    @Resource
    IPublicUserService publicUserService;

    @Resource
    IPublicUserFollowerService publicUserLikeService;

    @Resource
    ITransferService transferService;

    public final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 第三方调用的
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value="/thirdBind", method={RequestMethod.POST}, produces={MediaType.APPLICATION_JSON_UTF8_VALUE})
    public CallbackStatus thirdBind(@RequestBody String body, HttpServletRequest request) throws Exception {

        //多语言版本的例子
        PackBundle bundle = LangResource.getResourceBundle(request);

        CallbackStatus status = new CallbackStatus();

        logger.info("thirdBind_body=" + body);

        BindEntity entity = null;
        try{
            entity = JSON.parseObject(body, BindEntity.class);
        } catch (Exception ex){
            status.setStatus(1);
            status.setError(bundle.getString("need.json.error"));
            return status;
        }


        if(StringUtil.isEmptyOrBlank(entity.getUuid()) || StringUtil.isEmptyOrBlank(entity.getAppKey()) || StringUtil.isEmptyOrBlank(entity.getUniqueKey())){
            status.setStatus(1);
            status.setError(bundle.getString("need.json.error", "uuid/app_key/uniqueKey"));
            logger.info("uuid&app_key&uniqueKey does not allow null ");
            return status;
        }

        try{
            logger.info("用户绑定返回[BindEntity]：" + JSON.toJSONString(entity));
            //todo 需要验证身份证的正确性
            if(!userBindService.checkIfIdCardMatch(entity.getUuid(), entity.getUniqueKey())){
                status.setStatus(1);
                status.setError(bundle.getString("need.match.error"));
                logger.info(JSON.toJSON(entity) + ":用户提交绑定的uuid与返回身份证号不匹配");
                return status;
            }

            ThirdAppUserBindExt appUserBindExt = userBindService.getOneExtByCondition(entity.getUuid(), null, entity.getAppKey());
            CallbackStatus rst = userBindService.checkValidParam(bundle, appUserBindExt, entity);
            if(rst.getStatus() != 0){
                logger.info("return status=" + JSON.toJSONString(rst));
                return rst;
            }

            appUserBindExt.setUuid(entity.getUuid());
            appUserBindExt.setAppId(appUserBindExt.getAppId());
            appUserBindExt.setUniqueKey(entity.getUniqueKey());
            appUserBindExt.setUniqueAddress(entity.getUniqueAddress());
            appUserBindExt.setBindTime(entity.getBindTime());
            appUserBindExt.setUserKey(entity.getUserKey());
            appUserBindExt.setUserSecretKey(entity.getUserSecretKey());
            appUserBindExt.setBindStatus(1);


            try{
                focusAppUser(entity.getUuid(), appUserBindExt.getAppId());
            } catch (Exception ex){
                ex.printStackTrace();
            }
            //插入用户与平台绑定记录，建立绑定
            //如果绑定大于0，说明绑定成功，进行红包或转账资产转入操作
            int rval = userBindService.updateBinding(appUserBindExt);
            logger.info("用户绑定状态修改结果[updateBinding]：" + rval);
            if (rval > 0){
                final String uuid = entity.getUuid();
                // 实现runnable借口，创建线程并启动
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //业务执行休眠时间
                        long sleepTime = 2*60*1000;
                        try {
                            logger.info("自动奖励红包入账休眠2分钟，预计BCEX平台处理完成后执行业务");
                            Thread.sleep(sleepTime);
                            //处理绑定后的红包和入群奖励信息
                            logger.info("***************************************************");
                            logger.info("开始执行自动奖励红包入账业务");
                            logger.info("***************************************************");
                            transferService.prizeTransferAccount(bundle, uuid);
                            logger.info("***************************************************");
                            logger.info("执行自动奖励红包入账业务完成！");
                            logger.info("***************************************************");
                        }
                        catch (Exception e){
                            e.printStackTrace();
                            logger.info("首次绑定资产账户，自动奖励红包入账异常：" + e.getMessage());
                        }
                    }
                }) {
                }.start();

            }

        } catch(Exception ex){
            status.setStatus(1);
            status.setError(ex.getMessage());
            logger.info("return status=" + JSON.toJSONString(status));
            return status;
        }
        status.setStatus(0);
        logger.info("return status=" + JSON.toJSONString(status));
        return status;
    }

    /**
     * 绑定测试
     * @param uuid
     * @param request
     * @throws Exception
     */
    @RequestMapping(value="/bindtest", method={RequestMethod.GET}, produces={MediaType.APPLICATION_JSON_UTF8_VALUE})
    public void bindtest(String uuid, HttpServletRequest request) throws Exception {
        PackBundle bundle = LangResource.getResourceBundle(request);
        transferService.prizeTransferAccount(bundle,uuid);
    }

    private void focusAppUser(String uuid, String appKey){

        PublicUser appUser = publicUserService.getByAppId(appKey);
        if(appUser == null){
            return;
        }

        //todo 自动关注
        List<PublicUser> userList = publicUserService.getByUuid(uuid);

        for(PublicUser user : userList){
            publicUserLikeService.focusPublicUser(user.getId(), appUser.getId());
        }
    }


    @ResponseBody
    @RequestMapping(value="/unbind", method={RequestMethod.POST}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String unBind(@RequestBody String body, HttpServletRequest request){
        String apiName = "post + /api/money/unbind";

        PackBundle bundle = LangResource.getResourceBundle(request);

        UserDetails userDetails = SessionUtil.getUserDetails(request);
        if(null == userDetails) {
            return toJson(1, bundle.getString("user.unauthorized"), apiName, null);
        }


        String platformId = null;
        try{
            JSONObject param = JSONObject.parseObject(body);
            platformId = param.getString("platformId");
            if(StringUtil.isEmptyOrBlank(platformId)){
                return toJson(2, bundle.getString("need.param", "platformId"), apiName, null);
            }
        } catch(Exception ex){
            return toJson(2, bundle.getString("need.param.error"), apiName, null, ex.getMessage());
        }

        try {
            userBindService.updateUnBinding(bundle, userDetails.getUuid(), platformId);
            return toJson(0, "", apiName, null);
        }catch(DefinedError ex){
            return toJson(4, ex.getReadableMsg(), apiName, null, ex.getMessage());
        }catch(Exception ex){
            return toJson(5, bundle.getString("failed.execute"), apiName, null, ex.getMessage());
        }


    }
}
