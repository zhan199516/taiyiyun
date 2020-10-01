package com.taiyiyun.passport.controller.circle;

import com.alibaba.fastjson.JSON;
import com.taiyiyun.passport.po.PublicArticle;
import com.taiyiyun.passport.service.IPublicArticleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by okdos on 2017/7/28.
 * 将旧版本文章发布到服务器
 */
@Controller
public class ApiPublishOldArticle {
    @Resource
    private IPublicArticleService publicArticleService;
    public final Logger logger = LoggerFactory.getLogger(getClass());


    @ResponseBody
    @RequestMapping(value="/api/publishOldArticle", method = {RequestMethod.GET}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public String publishOldArticle(HttpServletRequest request){

        List<String> logList = new ArrayList<>();

        while(true){
            List<PublicArticle> list =  publicArticleService.getUnpublishOldArticle(null, null, null, 0);
            if(list.size() == 0){
                break;
            }

            logger.info("publishOldArticle---------->" + JSON.toJSON(list));

            for (PublicArticle article : list) {
                article.setOnlineStatus(2);
                publicArticleService.updateOnlineStatus(article);
                publicArticleService.publish(request, article, true);
                logList.add("ArticleId=" + article.getArticleId() + " publish finished");
            }
        }

        return JSON.toJSONString(logList);
    }

}
