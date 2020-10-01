package com.taiyiyun.passport.controller.circle;

import com.taiyiyun.passport.bean.UserDetails;
import com.taiyiyun.passport.language.LangResource;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.po.PublicArticle;
import com.taiyiyun.passport.po.PublicUser;
import com.taiyiyun.passport.service.IArticleViewService;
import com.taiyiyun.passport.service.IPublicArticleService;
import com.taiyiyun.passport.service.IPublicUserService;
import com.taiyiyun.passport.util.SessionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by okdos on 2017/6/28.
 */

@Controller
@RequestMapping("/view")
public class ArticleController {

    public final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    IArticleViewService service;
    
    @Resource
    IPublicArticleService articleService;
    
    @Resource
    IPublicUserService userService;

    @RequestMapping("/ArticleView")
    public String getArticleOld(String article_id, String message_id, String method, Model model, HttpServletRequest request){



        try{
            //logger.info("article_id=" + article_id);
            if(message_id != null){
                article_id = message_id;
            }
            return getArticle(article_id, model, request);
        }catch (Exception ex){
            logger.error(ex.getMessage());
            ex.printStackTrace();
            return "errors/500";
        }

    }

    //@RequestMapping("/article")
    private String getArticle(String articleId, Model model, HttpServletRequest request){

        //语言设置
        PackBundle bundle = LangResource.getResourceBundle(request);

        String viewId = "";

        //todo 正式版本中去掉
        UserDetails userDetails = SessionUtil.getUserDetails(request);
        if(null != userDetails) {
            viewId = userDetails.getUserId();
        }

        try{

            //logger.info("111111111111111111111111");

            HashMap<String, Object> map = service.readArticle(articleId, viewId);

            //logger.info("2222222222222");

            if(map.size() == 0){
                //logger.info("3333333333333333333333");
                return "errors/404";
            } else {
                for(Map.Entry<String, Object> entry: map.entrySet()){
                    model.addAttribute(entry.getKey(), entry.getValue());
                }

                //logger.info(JSON.toJSONString(map));

                //

                return "article_" + bundle.getLanguage();

                // "article";
            }
        } catch (Exception ex){
            logger.error(ex.getMessage());
        	ex.printStackTrace();
            return "errors/500";
        }

    }
    
	@RequestMapping("/CertificateView")
	public String viewCertificate(String article_id, Model model, HttpServletRequest request) {
		try {
            PackBundle bundle = LangResource.getResourceBundle(request);
			PublicArticle article = articleService.getById(article_id);
			if (null != article && article.getIsOriginal()) {
				PublicUser user = userService.getByUserId(article.getUserId());
				model.addAttribute("user", user);
				model.addAttribute("article", article);
				return "certificate_" + bundle.getLanguage();
			}else if(null != article && !article.getIsOriginal()) {
				model.addAttribute("errorMsg", "该文章非原创作品");
				return "errors/error";
			}
			
			return "errors/404";
		} catch (Exception e) {
			e.printStackTrace();
			return "errors/500";
		}

	}

}
