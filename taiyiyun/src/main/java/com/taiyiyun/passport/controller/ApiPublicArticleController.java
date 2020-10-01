package com.taiyiyun.passport.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.taiyiyun.passport.bean.FileBean;
import com.taiyiyun.passport.bean.ImageBean;
import com.taiyiyun.passport.bean.UserDetails;
import com.taiyiyun.passport.consts.Const;
import com.taiyiyun.passport.consts.EnumStatus;
import com.taiyiyun.passport.language.LangResource;
import com.taiyiyun.passport.language.PackBundle;
import com.taiyiyun.passport.mqtt.Message;
import com.taiyiyun.passport.po.*;
import com.taiyiyun.passport.service.*;
import com.taiyiyun.passport.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
public class ApiPublicArticleController extends BaseController {

	public final Logger logger = LoggerFactory.getLogger(getClass());

	@Resource
	private IPublicArticleService publicArticleService;
	
	@Resource
	private IPublicUserService publicUserService;
	
	@Resource
	private IPublicArticleAccuseService articleAccuseService;
	
	@Resource
	private ICodeDictionaryService codeDictionaryService;
	
	@Resource
	private IThirdArticlePublishService thirdArticlePublishService;

	@Resource
	private IArticleViewService articleViewService;

	/**
	 * 发表文章
	 * @param tempId
	 * @param title
	 * @param content
	 * @param summary
	 * @param isOriginal
	 * @param topLevel 置顶级别，（未来考虑置顶级别的权限，不是什么人都有置顶能力）
	 * @param forwardFrom
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/api/article/create",method = {RequestMethod.POST}, produces = {Const.PRODUCES_JSON})
	public String newArticle(String tempId, String title, String content, String summary, String isOriginal, Integer topLevel,
			String forwardFrom, HttpServletRequest request) {
		UserDetails userDetails = SessionUtil.getUserDetails(request);

		PackBundle bundle = LangResource.getResourceBundle(request);
		String apiName = "post + /api/article/create";

		if(null == userDetails) {
			return toJson(1, bundle.getString("user.not.login"), apiName, new ArrayList<>());
		}
		
		if (StringUtil.isNotEmpty(title) && title.length() > 256) {
			return toJson(2, bundle.getString("article.title.length.alert", 256), apiName, new ArrayList<>());
		}
		
		if(StringUtil.isNotEmpty(summary) && summary.length() > 1000) {
			return toJson(4, bundle.getString("article.summary.length.alert", 1000), apiName, new ArrayList<>());
		}
				
		try {
			PublicUser user = publicUserService.getByStrictId(userDetails.getUuid(), userDetails.getUserId());
			if(null == user) {
				return toJson(3, bundle.getString("user.public.not.find"), apiName, new ArrayList<>());
			}

			PublicArticle article = new PublicArticle();
			article.setUserId(userDetails.getUserId());
			article.setUpdateTime(new Date());
			article.setPublishTime(new Date());
			article.setContent(content);
			article.setTitle(title);
			article.setSummary(summary);
			article.setForwardFrom(forwardFrom);
			article.setContentType(501);
			article.setOnlineStatus(2);
			article.setIsOriginal(false);
			if(StringUtil.isNotEmpty(isOriginal) && (isOriginal.equals("1") || isOriginal.equals("true"))) {
				article.setIsOriginal(true);
			}
			
			if(null != article.getIsOriginal() && article.getIsOriginal()) {
				article.setRegisterNo(Misc.getBCIPC());
			}
			
			List<FileBean> beans = generateThumbImg(tempId, request);
			if(null != beans && beans.size() > 0) {
				JSONArray jsonList = new JSONArray();
				for(int i = 0; i <  beans.size(); i++) {
					FileBean fileBean = beans.get(i);
					jsonList.add(fileBean.getRelativePath());
				}
				article.setThumbImg(jsonList.toString());
				if (jsonList.size() <= 2) {
					article.setContentType(503);
				} else if (jsonList.size() >= 3) {
					article.setContentType(504);
				}
			}

			String secret = "title" + article.getTitle() + "summary" + article.getSummary() + "content" + article.getContent();
			article.setArticleHash(SHAUtil.sha256(secret, true));
			publicArticleService.save(article);
			publicArticleService.publish(request, article, false);
			//publicArticleService.pushALiMessage(article, user);
			return toJson(0, bundle.getString("successful.article.publish"), apiName, new ArrayList<>());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			if(e.getMessage().contains("Incorrect string value")) {
				return toJson(9, bundle.getString("failed.save.emoji"), apiName, null);
			}
			return toJson(5, bundle.getString("failed.execute"), apiName, new ArrayList<>());
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/api/article/uploadImage", method = {RequestMethod.POST}, produces = { Const.PRODUCES_JSON })
	public String uploadArticleImage(String imageId, String tempId, MultipartFile imageFile, HttpServletRequest request) {

		PackBundle bundle = LangResource.getResourceBundle(request);
		String apiName = "post + /api/article/uploadImage";
		
		if(FileUtil.multipartFileIsNull(imageFile)){
			return toJson(2,bundle.getString("need.img"), apiName, new ArrayList<>());
		}
		
		try {
			FileBean imageBean = FileUtil.saveFile(request, false, "/files/images/articles", imageFile, ".PNG|.JPG|.GIF|.JPEG|.BMP");
			
			if("1".equals(imageId) || "2".equals(imageId) || "3".equals(imageId)) {
				if(StringUtil.isEmpty(tempId)){
					return toJson(3,bundle.getString("need.param", "tempId"), apiName, new ArrayList<>());
				}
				
				List<ImageBean> thumbnailImageList = CacheUtil.getHalfHour(tempId);
				if(null == thumbnailImageList) {
					thumbnailImageList = new LinkedList<ImageBean>();
				}
				
				ImageBean bean = new ImageBean();
				bean.setId(imageId);
				bean.setRealPath(imageBean.getAbsolutePath());
				bean.setExtName(imageBean.getExtName());
				bean.setFile(imageBean.getFile());
				
				thumbnailImageList.add(bean);
				CacheUtil.putHalfHour(tempId, thumbnailImageList);
			}
			List<Map<String,Object>> jsonList = new ArrayList<>();
			Map<String,Object> jsonMap = new HashMap<String,Object>();
			jsonMap.put("imageUrl", imageBean.getRelativePath());
			jsonList.add(jsonMap);
			
			return toJson(0,bundle.getString("successful.upload"), apiName, jsonList);
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(4, bundle.getString("failed.execute"), apiName, new ArrayList<>());
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/api/article/getArticleList", method = {RequestMethod.GET}, produces = {Const.PRODUCES_JSON})
	public String getArticles(String shareId,HttpServletRequest request){
		UserDetails userDetails = SessionUtil.getUserDetails(request);

		PackBundle bundle = LangResource.getResourceBundle(request);
		String apiName = "get + /api/article/getArticleList";

		if(null == userDetails) {
			return toJson(1, bundle.getString("user.not.login"), apiName, new ArrayList<>());
		}
		
		try {
			return toJson(0, bundle.getString("successful.search"), apiName, publicArticleService.getByUserId(userDetails.getUserId()));
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(2, bundle.getString("failed.execute"), apiName, new ArrayList<>());
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/api/article/getArticle", method = {RequestMethod.GET}, produces = {Const.PRODUCES_JSON})
	public String getArticleById(String articleId,HttpServletRequest request){
		UserDetails userDetails = SessionUtil.getUserDetails(request);

		PackBundle bundle = LangResource.getResourceBundle(request);
		String apiName = "get + /api/article/getArticle";

		if(null == userDetails) {
			return toJson(1, bundle.getString("user.not.login"), apiName, new ArrayList<>());
		}
		
		try {
			return toJson(0, bundle.getString("successful.search"), apiName, publicArticleService.getById(articleId));
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(2, bundle.getString("failed.execute"), apiName, new ArrayList<>());
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/api/article/delete", method = {RequestMethod.POST}, produces = {Const.PRODUCES_JSON})
	public String deleteArticles(String articleId, HttpServletRequest request) {
		UserDetails userDetails = SessionUtil.getUserDetails(request);

		PackBundle bundle = LangResource.getResourceBundle(request);
		String apiName = "post + /api/article/delete";

		if(null == userDetails) {
			return toJson(1, bundle.getString("user.not.login"), apiName, new ArrayList<>());
		}
		
		if(StringUtil.isEmpty(articleId)) {
			return toJson(2, bundle.getString("need.param", "articleId"), apiName, new ArrayList<>());
		}
		
		if(StringUtil.isEmpty(userDetails.getUserId())) {
			return toJson(5, bundle.getString("user.not.login"), apiName, new ArrayList<>());
		}
		
		try {
			PublicArticle article = publicArticleService.getById(articleId);
			if(null == article) {
				return toJson(6, bundle.getString("article.has.been.deleted"), apiName, new ArrayList<>());
			}
			
			int count = publicArticleService.deleteArticles(request, userDetails.getUserId(), article);
			if(count == 1) {
				return toJson(0, bundle.getString("successful.delete"), apiName, new ArrayList<>());
			}
			return toJson(4, bundle.getString("failed.delete"), apiName, new ArrayList<>());
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(3, bundle.getString("failed.execute"), apiName, new ArrayList<>());
		}
	}


	/**
	 * 第三方发布文章
	 */
	@ResponseBody
	@RequestMapping(value = "/api/article/publish",method = {RequestMethod.POST}, produces = {Const.PRODUCES_JSON})
	public String doCreateArticle(String title, String content, String summary, String isOriginal, String forwardFrom,
			String appKey, String sign, HttpServletRequest request) {

		PackBundle bundle = LangResource.getResourceBundle(request);
		String apiName = "post + /api/article/create";

		if(StringUtil.isEmpty(title)) {
			return toJson(2, bundle.getString("need.article.title"), apiName, new ArrayList<>());
		}
		
		if(title.length() > 256) {
			return toJson(2, bundle.getString("article.title.length.alert", 256), apiName, new ArrayList<>());
		}
		
		if(StringUtil.isEmpty(sign)) {
			return toJson(3, bundle.getString("need.sign"), apiName, new ArrayList<>());
		}
		
		if(StringUtil.isEmpty(appKey)) {
			return toJson(4, bundle.getString("need.param", "appKey"), apiName, new ArrayList<>());
		}
				
		try {
			ThirdArticlePublish thirdApp = thirdArticlePublishService.getByAppKey(appKey);
			if(null == thirdApp) {
				return toJson(5, bundle.getString("article.appkey.match"), apiName, new ArrayList<>());
			}
			
			Map<String, String> params = new HashMap<>();
			params.put("title", title);
			params.put("content", content);
			params.put("summary", summary);
			params.put("isOriginal", isOriginal);
			params.put("forwardFrom", forwardFrom);
			params.put("appKey", appKey);
			
			String dataSign = MD5Signature.signMd5(params, thirdApp.getAppSecret());
			
			if(!sign.equals(dataSign)) {
				return toJson(6, bundle.getString("failed.sign"), apiName, new ArrayList<>());
			}
			
			PublicUser user = publicUserService.getByAppId(thirdApp.getAppId());
			if(null == user) {
				return toJson(7, bundle.getString("user.public.not.find"), apiName, new ArrayList<>());
			}
			
			PublicArticle article = new PublicArticle();
			article.setUserId(user.getId());
			article.setUpdateTime(new Date());
			article.setPublishTime(new Date());
			article.setContent(content);
			article.setTitle(title);
			article.setSummary(summary);

			String imageUrl = articleViewService.findFirstImageUrl(content);
			if(imageUrl == null){
				article.setContentType(Message.ContentType.CONTENT_CIRCLE_GENERIC_TITLE_TEXT.getValue());
			} else {
				article.setContentType(Message.ContentType.CONTENT_CIRCLE_GENERIC_ONE_IMAGE.getValue());
				article.setThumbImg(JSON.toJSONString(Arrays.asList(imageUrl)));
			}

			article.setOnlineStatus(2);
			article.setIsOriginal(false);
			if(StringUtil.isNotEmpty(isOriginal) && (isOriginal.equals("1") || isOriginal.equals("true"))) {
				article.setIsOriginal(true);
			}
			
			if(null != article.getIsOriginal() && article.getIsOriginal()) {
				article.setRegisterNo(Misc.getBCIPC());
				article.setForwardFrom(forwardFrom);
			}

			String secret = "title" + title + "summary" + summary + "content" + content;
			article.setArticleHash(SHAUtil.sha256(secret, true));
			
			publicArticleService.save(article);
			publicArticleService.publish(request, article, false);
			//publicArticleService.pushALiMessage(article, user);
			
			return toJson(0, bundle.getString("successful.article.publish"), apiName, new ArrayList<>());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			if(e.getMessage().contains("Incorrect string value")) {
				return toJson(9, bundle.getString("failed.save.emoji"), apiName, null);
			}
			return toJson(9, bundle.getString("failed.execute"), apiName, null);
		}
	}
	
	/**
	 * 文章举报
	 */
	@ResponseBody
	@RequestMapping(value = "/api/article/accuse", method = {RequestMethod.POST}, produces = {Const.PRODUCES_JSON})
	public String accuseArticles(String articleId, Integer accuseTypeId, String accuseContent, HttpServletRequest request) {
		PackBundle bundle = LangResource.getResourceBundle(request);
		String apiName = "post + /api/article/create";

		UserDetails userDetails = SessionUtil.getUserDetails(request);
		if(null == userDetails) {
			return toJson(1, bundle.getString("user.not.login"), apiName, new ArrayList<>());
		}
		
		if(StringUtil.isEmpty(articleId) || accuseTypeId == null) {
			return toJson(2, bundle.getString("need.param", ""), apiName, new ArrayList<>());
		}
		
		if(StringUtil.isEmpty(userDetails.getUserId())) {
			return toJson(3, bundle.getString("user.not.login"), apiName, new ArrayList<>());
		}
		
		try {
			PublicArticle article = publicArticleService.getById(articleId);
			if(null == article) {
				return toJson(4, bundle.getString("article.has.been.deleted"), apiName, new ArrayList<>());
			}
			
			CodeDictionary codeDictionary = codeDictionaryService.getById(accuseTypeId);
			if(null == codeDictionary) {
				return toJson(5, bundle.getString("article.alert.type.error"), apiName, new ArrayList<>());
			}
			
			PublicArticleAccuse articleAccuse = new PublicArticleAccuse();
			articleAccuse.setUserId(userDetails.getUserId());
			articleAccuse.setTargetArticleId(articleId);
			articleAccuse.setAccuseDescription(accuseContent);
			articleAccuse.setAccuseTypeId(accuseTypeId);
			articleAccuse.setAccuseTime(new Date());
			
			int count = articleAccuseService.accuseArticle(articleAccuse);
			return toJson(0, count == 1 ? bundle.getString("successful.article.alert") : bundle.getString("article.alert.done"), apiName, new ArrayList<>());
			
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(6, bundle.getString("failed.execute"), apiName, new ArrayList<>());
		}
	}
	
	/**
	 * 获取文章举报类型
	 */
	@ResponseBody
	@RequestMapping(value = "/api/article/getAccuseType", method = {RequestMethod.GET}, produces = {Const.PRODUCES_JSON})
	public String getAccuseArticles(@RequestParam(value = "businessType", required = false) String businessType,HttpServletRequest request) {
		PackBundle bundle = LangResource.getResourceBundle(request);
		String apiName = "get + /api/article/getAccuseType";
		try {
			//业务参数类型为空，默认为文章类型
			if(StringUtil.isEmpty(businessType)){
				businessType = "public_article_accuse";
			}
			List<CodeDictionary> dataList = codeDictionaryService.getByBusiness(businessType + "_" + bundle.getLanguage());
			List<Map<String, Object>> jsonList = new ArrayList<>();
			
			if (null != dataList && dataList.size() > 0) {
				for (CodeDictionary cd : dataList) {
					Map<String, Object> json = new HashMap<>();
					json.put("id", cd.getId());
					json.put("code", cd.getCode());
					json.put("text", cd.getCaption());
					jsonList.add(json);
				}
			}
			
			return toJson(0, bundle.getString("successful.search"), apiName, jsonList);
		} catch (Exception e) {
			e.printStackTrace();
			return toJson(EnumStatus.NINETY_NINE.getIndex(), bundle.getString("failed.execute"), apiName, new ArrayList<>());
		}
	}
	
	private List<FileBean> generateThumbImg(String tempId, HttpServletRequest request) {
		if(StringUtil.isEmpty(tempId)) {
			return null;
		}
		
		List<ImageBean> thumbnailImageList = CacheUtil.getHalfHour(tempId);
		if(null == thumbnailImageList || thumbnailImageList.size() <= 0) {
			return null;
		}
		
		List<FileBean> fileBeanList = new ArrayList<FileBean>();
		try {
			for (ImageBean imagebean : thumbnailImageList) {

				FileBean bean = FileUtil.getArticleThumbnail(request, false, "files/images/thumbnail/article", imagebean.getFile(), ".PNG|.JPG|.GIF|.JPEG|.BMP");
				fileBeanList.add(bean);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return fileBeanList;
	}
}
