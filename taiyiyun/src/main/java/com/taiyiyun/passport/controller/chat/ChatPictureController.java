package com.taiyiyun.passport.controller.chat;

import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import com.taiyiyun.passport.aliyun.sts.StsServiceSample;
import com.taiyiyun.passport.bean.CollectExpressionBean;
import com.taiyiyun.passport.bean.UserDetails;
import com.taiyiyun.passport.controller.BaseController;
import com.taiyiyun.passport.po.chat.ChatPicture;
import com.taiyiyun.passport.po.chat.CollectExpression;
import com.taiyiyun.passport.po.chat.ExpActionLog;
import com.taiyiyun.passport.service.ChatPictureService;
import com.taiyiyun.passport.service.CollectExpressionService;
import com.taiyiyun.passport.service.ExpActionLogService;
import com.taiyiyun.passport.util.HttpClientUtil;
import com.taiyiyun.passport.util.SessionUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;


@Controller
@RequestMapping("/api/chat")
public class ChatPictureController extends BaseController {

    private static Log logger = LogFactory.getLog(ChatPictureController.class);

    private String aliyunHost = "http://gxhz.oss-cn-beijing.aliyuncs.com/";

    @Resource
    private ChatPictureService chatPictureService;

    @Resource
    private CollectExpressionService collectExpressionService;

    @Resource
    private ExpActionLogService expActionLogService;


    /**
     * 检查文件是否存在
     *
     * @param request
     * @param response
     * @return
     * @throws IOException
     */

    @ResponseBody
    @RequestMapping(value = "/getCredentials", method = {RequestMethod.GET}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String checkPicture(HttpServletRequest request, HttpServletResponse response) {
        String apiName = "get + /api/chat/getCredentials";
        Map<String, Object> json = new HashMap<>();
        
        try {


            AssumeRoleResponse assumeRoleResponse = StsServiceSample.assumeRole();

            json.put("callback_url", request.getScheme()+"://"+request.getServerName()+":"+ request.getServerPort()+"/api/chat/uploadCallback");
            json.put("credentials", assumeRoleResponse.getCredentials());

            return toJson(0, "", apiName,  json, false);

        } catch (Exception ex) {

            ex.printStackTrace();
            logger.error(ex.getMessage());
            return toJson(1, "调用失败", apiName, new ArrayList<>(), false);

        }

    }


    /**
     * 收藏表情
     *
     * @param request
     * @param response
     * @return
     */

    @ResponseBody
    @RequestMapping(value = "/expression/add", method = {RequestMethod.POST}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String addFace(HttpServletRequest request, HttpServletResponse response) {
        String apiName = "post + /api/chat/expression/add";
        Map<String, Object> json = new HashMap<>();
        try {
            String picName = request.getParameter("picMd5");
            String pic[] = picName.split("\\.");
            String picMd5 = pic[0];

            UserDetails userDetails = SessionUtil.getUserDetails(request);
            String userId = userDetails.getUserId();

            ChatPicture chatPicture = chatPictureService.getByMd5(picMd5);

            if (null == chatPicture) {
                Map<String, String> param = new HashMap<>();
                param.put("x-oss-process", "image/info");
                String url = aliyunHost + picName;

                String rst = HttpClientUtil.doGet(url, param);

                ObjectMapper mapper = new ObjectMapper();
                Map m = mapper.readValue(rst, Map.class);
                Map imageHeight = (Map) m.get("ImageHeight");
                Map imageWidth = (Map) m.get("ImageWidth");

                chatPicture = new ChatPicture();
                chatPicture.setPicMd5(picMd5);
                chatPicture.setPicHeight(imageHeight.get("value").toString());
                chatPicture.setPicWidth(imageWidth.get("value").toString());
                chatPicture.setPicName(picName);
                Date date = new Date();
                chatPicture.setCreateTime(date);
                chatPictureService.save(chatPicture);
            }

            CollectExpressionBean collectExpressionBean =new CollectExpressionBean();
            CollectExpression collectExpression = new CollectExpression();
            collectExpression.setPicMd5(picMd5);
            collectExpression.setUserId(userId);
            collectExpression.setCreateTime(new Date());
            collectExpressionService.save(collectExpression);
            Integer expId = collectExpression.getId();


            ExpActionLog expActionLog = new ExpActionLog();
            expActionLog.setUserId(userId);
            expActionLog.setCollectExpId(expId);
            expActionLog.setAction(1);
            expActionLog.setCreateTime(new Date().getTime());
            List<ExpActionLog> expActionLogList = new ArrayList<>();
            expActionLogList.add(expActionLog);
            expActionLogService.save(expActionLogList);

            collectExpressionBean.setId(expId);
            collectExpressionBean.setPicHeight(chatPicture.getPicHeight());
            collectExpressionBean.setPicWidth(chatPicture.getPicWidth());
            collectExpressionBean.setPicMd5(chatPicture.getPicMd5());
            collectExpressionBean.setPicName(chatPicture.getPicName());
            collectExpressionBean.setCreateTime(collectExpression.getCreateTime());
            json.put("collectExpression",collectExpressionBean);
            json.put("picHost",aliyunHost);
            return toJson(0, "", apiName,  json, false);

        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage());
        }

        return toJson(1, "添加失败", apiName, json, false);
    }

    /**
     * 移除表情
     *
     * @param request
     * @param response
     * @return
     */

    @ResponseBody
    @RequestMapping(value = "/expression/del", method = {RequestMethod.POST}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String delFace(HttpServletRequest request, HttpServletResponse response) {
        String apiName = "post + /api/chat/expression/del";
        Map<String, Object> json = new HashMap<>();

        long time = Calendar.getInstance().getTimeInMillis();
        json.put("time",String.valueOf(time));
        try {
           String ids =  request.getParameter("id");
            String idArray[] =ids.split("\\,");
            List<Integer> idList = new ArrayList<>();
            for(int i=0;i<idArray.length;i++){

                idList.add(Integer.valueOf(idArray[i]));
            }


            UserDetails userDetails = SessionUtil.getUserDetails(request);
            String userId =userDetails.getUserId();


            collectExpressionService.delExpression(idList,userId);
            List<ExpActionLog> expActionLogList = new ArrayList<>();

            for(int i=0;i<idList.size();i++){

                ExpActionLog expActionLog = new ExpActionLog();
                expActionLog.setUserId(userId);
                expActionLog.setCollectExpId(idList.get(i));
                expActionLog.setAction(2);
                expActionLog.setCreateTime(new Date().getTime());
                expActionLogList.add(expActionLog);
            }

            expActionLogService.save(expActionLogList);

            return toJson(0, "", apiName,  json, false);

        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage());
        }
        return toJson(1, "", apiName,  json, false);
    }


    /**
     * 获取收藏列表
     *
     * @param request
     * @param response
     * @return
     */

    @ResponseBody
    @RequestMapping(value = "/getUserExpList", method = {RequestMethod.GET}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String getUserExpList(HttpServletRequest request, HttpServletResponse response) {
        String apiName = "get + /api/chat/getUserExpList";
        Map<String, Object> json = new HashMap<>();

        long nowTime = Calendar.getInstance().getTimeInMillis();
        json.put("time",String.valueOf(nowTime));


        try {
            String time = request.getParameter("time");

            UserDetails userDetails = SessionUtil.getUserDetails(request);
            String userId =userDetails.getUserId();


            List<CollectExpressionBean> chatPictures = new ArrayList<CollectExpressionBean>();
            List<Integer> delChatPictureList = new ArrayList<Integer>();

            if (time.equals("0")) {
                List<CollectExpression> collectExpressions = collectExpressionService.getByUserId(userId);
                for (int i = 0; i < collectExpressions.size(); i++) {
                    CollectExpression collectExpression = collectExpressions.get(i);
                    String picMd5 = collectExpression.getPicMd5();
                    ChatPicture chatPicture = chatPictureService.getByMd5(picMd5);
                    CollectExpressionBean collectExpressionBean =  new CollectExpressionBean();

                    collectExpressionBean.setId(collectExpression.getId());
                    collectExpressionBean.setPicMd5(chatPicture.getPicMd5());
                    collectExpressionBean.setPicName(chatPicture.getPicName());
                    collectExpressionBean.setPicWidth(chatPicture.getPicWidth());
                    collectExpressionBean.setPicHeight(chatPicture.getPicHeight());

                    chatPictures.add(collectExpressionBean);
                }

                json.put("add", chatPictures);
            } else {
                Long pDate = Long.valueOf(time);
                List<ExpActionLog> expActions = expActionLogService.getByTime(userId,pDate);
                for (int j = 0; j < expActions.size(); j++) {
                    ExpActionLog expAction = expActions.get(j);
                    if (expAction.getAction() == 1) {
                        Integer expId = expAction.getCollectExpId();


                        CollectExpression collectExpression = collectExpressionService.getById(expId);

                        if (null == collectExpression) {
                            continue;
                        }

                        String picMd5 = collectExpression.getPicMd5();
                        ChatPicture chatPicture = chatPictureService.getByMd5(picMd5);


                        CollectExpressionBean collectExpressionBean =  new CollectExpressionBean();
                        collectExpressionBean.setId(collectExpression.getId());
                        collectExpressionBean.setPicMd5(chatPicture.getPicMd5());
                        collectExpressionBean.setPicName(chatPicture.getPicName());
                        collectExpressionBean.setPicWidth(chatPicture.getPicWidth());
                        collectExpressionBean.setPicHeight(chatPicture.getPicHeight());

                        chatPictures.add(collectExpressionBean);
                    } else if (expAction.getAction() == 2) {
                        delChatPictureList.add(expAction.getCollectExpId());
                    }

                }

                json.put("add", chatPictures);
                json.put("del", delChatPictureList);

            }

            json.put("picHost",aliyunHost);
            return toJson(0, "", apiName,  json, false);

        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage());
        }
        return toJson(1, "", apiName,  json, false);
    }


    /**
     * 上传回调
     *
     * @param request
     * @param response
     * @return
     * @throws IOException
     */

    @ResponseBody
    @RequestMapping(value = "/uploadCallback", method = {RequestMethod.POST})
    public String uploadCallback(HttpServletRequest request, HttpServletResponse response){
        try {
            ChatPicture chatPicture = new ChatPicture();
            String imageHeight = request.getParameter("imageInfo.height");
            String imageWidth = request.getParameter("imageInfo.width");
            String picName = request.getParameter("object");
            String pic[] = picName.split("\\.");
            String picMd5 = pic[0];
            chatPicture.setPicMd5(picMd5);
            chatPicture.setPicHeight(imageHeight);
            chatPicture.setPicWidth(imageWidth);
            chatPicture.setPicName(picName);
            Date date = new Date();
            chatPicture.setCreateTime(date);
            chatPictureService.save(chatPicture);
            return toJson("200", "");
        }catch (Exception ex){
            ex.printStackTrace();
            logger.error(ex.getMessage());
        }
        return toJson("203", "");

    }

}
