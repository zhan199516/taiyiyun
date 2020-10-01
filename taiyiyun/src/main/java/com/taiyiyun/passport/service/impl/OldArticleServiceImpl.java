package com.taiyiyun.passport.service.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.taiyiyun.passport.dao.IOldArticleDao;
import com.taiyiyun.passport.po.PublicArticle;
import com.taiyiyun.passport.po.immigrate.OldArticle;
import com.taiyiyun.passport.service.IOldArticleService;
import com.taiyiyun.passport.util.FileUtil;
import com.taiyiyun.passport.util.StringUtil;

@Service
public class OldArticleServiceImpl implements IOldArticleService {

	@Resource
	private IOldArticleDao dao;

	@Override
	public Long startTransferArticleImage(HttpServletRequest request, Long id) {
		
		Result rs = doTransferArticleImage(request, id);
		Long count = rs.getCount();
		
		while (null != rs.getMaxId() && rs.getMaxId() > 0) {
			
			rs = doTransferArticleImage(request, rs.getMaxId());
			count = count + rs.getCount();
			
		}
		
		return count;
	}

	public Result doTransferArticleImage(HttpServletRequest request, Long id) {
		List<OldArticle> dataList = dao.getByUUID(id);
		Result rs = new Result();
		Long count = 0L;
		
		if (null != dataList && dataList.size() > 0) {

			for (OldArticle old : dataList) {
				
				rs.setMaxId(Long.parseLong(old.getArticleId()));
				
				if (StringUtil.isNotEmpty(old.getImageType()) && null != old.getImage()) {
					try {

						ByteArrayInputStream bais = new ByteArrayInputStream(old.getImage());
						BufferedImage bi1 = ImageIO.read(bais);
						String imageType = FileUtil.getFileTypeByByte(old.getImage());
						String relativePath = old.getImagePath() + "." + imageType;
						String filePath = request.getSession().getServletContext().getRealPath(relativePath);
						File w2 = new File(filePath);// 可以是jpg,png,gif格式

						if (!w2.exists()) {
							w2.mkdirs();
						}

						ImageIO.write(bi1, imageType, w2);// 不管输出什么格式图片，此处不需改动

						//if (old.getImageType().contains("*")) {
						PublicArticle article = new PublicArticle();
						article.setArticleId(old.getArticleId());
						article.setThumbImg(JSONObject.toJSONString(Arrays.asList(relativePath)));
						dao.updateArticle(article);

						System.out.println("id: " + old.getArticleId() + "  path: " + relativePath);
						//}
						count++;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		rs.setCount(count);
		return rs;
	}
	
	public static class Result{
		private Long maxId;
		
		private Long count;

		public Long getMaxId() {
			return maxId;
		}

		public void setMaxId(Long maxId) {
			this.maxId = maxId;
		}

		public Long getCount() {
			return count;
		}

		public void setCount(Long count) {
			this.count = count;
		}
	}

}
