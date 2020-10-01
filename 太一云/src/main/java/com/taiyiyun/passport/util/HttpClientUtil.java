package com.taiyiyun.passport.util;

import com.alibaba.fastjson.JSONObject;
import com.taiyiyun.passport.exception.DefinedError;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.CharsetUtils;
import org.apache.http.util.EntityUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.Map.Entry;

public class HttpClientUtil {

	public static void main(String[] args) {
		String url = "http://220.194.43.231:8080/rna/api/realNameAuthTwoInfo";
		Map<String, String> params = new HashMap<>();
		params.put("idNumber", "130221198504250010");
		try {
			String s = doPost(url, params, 10000);
			System.out.println(s);
		} catch (DefinedError definedError) {
			definedError.printStackTrace();
		}
	}

	public static String doPost(String url, Map<String, String> params, int timeOut) throws DefinedError{
		RequestConfig defaultRequestConfig = RequestConfig.custom().setSocketTimeout(timeOut).setConnectTimeout(timeOut).setConnectionRequestTimeout(5000).build();
		CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(defaultRequestConfig).build();
		String responseText = null;
		CloseableHttpResponse response = null;
		try {
			HttpPost method = new HttpPost(url);

			if (params != null && params.size() > 0) {
				List<NameValuePair> paramList = new ArrayList<NameValuePair>();
				for (Map.Entry<String, String> param : params.entrySet()) {
					NameValuePair pair = new BasicNameValuePair(param.getKey(), param.getValue());
					paramList.add(pair);
				}
				method.setEntity(new UrlEncodedFormEntity(paramList, "UTF-8"));
			}

			response = httpClient.execute(method);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				responseText = EntityUtils.toString(entity, "UTF-8");
			}

		} catch (Exception e) {
			e.printStackTrace();

			throw new DefinedError.ThirdErrorException("向第三方平台请求失败", e.getMessage());
		}
		return responseText;
	}

	public static String doPost(String url, Map<String, String> params) throws DefinedError {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		String responseText = null;
		CloseableHttpResponse response = null;

		try {
			HttpPost method = new HttpPost(url);

			if (params != null && params.size() > 0) {
				List<NameValuePair> paramList = new ArrayList<NameValuePair>();
				for (Map.Entry<String, String> param : params.entrySet()) {
					NameValuePair pair = new BasicNameValuePair(param.getKey(), param.getValue());
					paramList.add(pair);
				}
				method.setEntity(new UrlEncodedFormEntity(paramList, "UTF-8"));
			}

			response = httpClient.execute(method);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				responseText = EntityUtils.toString(entity, "UTF-8");
			}

		} catch (Exception e) {
			e.printStackTrace();

			throw new DefinedError.ThirdErrorException("向第三方平台请求失败", e.getMessage());
		}

		return responseText;
	}

	public static String doGet(String url, Map<String, String> params) throws DefinedError{

		if (StringUtil.isEmpty(url)) {
			throw new RuntimeException("url is null");
		}

		CloseableHttpClient httpClient = HttpClients.createDefault();
		String responseText = null;
		CloseableHttpResponse response = null;

		try {

			String serializeParams = "";
			if (params != null && params.size() > 0) {
				List<NameValuePair> paramList = new ArrayList<NameValuePair>();
				for (Map.Entry<String, String> param : params.entrySet()) {
					NameValuePair pair = new BasicNameValuePair(param.getKey(), param.getValue());
					paramList.add(pair);
				}
				serializeParams = EntityUtils.toString(new UrlEncodedFormEntity(paramList, "UTF-8"));
			}

			if(params != null && !params.isEmpty()) {
				if (url.contains("?") && url.endsWith("?")) {
					url = url + serializeParams;
				} else if (url.contains("?")) {
					url = url + "&" + serializeParams;
				} else {
					url = url + "?" + serializeParams;
				}
			}

			HttpGet method = new HttpGet(url);

			response = httpClient.execute(method);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				responseText = EntityUtils.toString(entity, "UTF-8");
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new DefinedError.ThirdErrorException("向第三方请求失败", e.getMessage());
		}

		return responseText;
	}
	
	public static String doUpload(String url, Map<String, Object> params) throws DefinedError{
		CloseableHttpClient httpClient = HttpClients.createDefault();
		String responseText = null;
		CloseableHttpResponse response = null;

		try {
			HttpPost method = new HttpPost(url);

			if (params != null && params.size() > 0) {
				MultipartEntityBuilder builder = MultipartEntityBuilder.create();
				
				Iterator<Entry<String, Object>> it = params.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, Object> entry = it.next();
					if(entry.getValue() instanceof MultipartFile) {
						MultipartFile multipartFile = (MultipartFile)entry.getValue();
						builder.addBinaryBody(entry.getKey(), multipartFile.getInputStream(), ContentType.MULTIPART_FORM_DATA, multipartFile.getOriginalFilename());
					}else if(entry.getValue() instanceof String) {
						
						builder.addPart(entry.getKey(), new StringBody(String.valueOf(entry.getValue()), ContentType.create("text/plain", Consts.UTF_8)));
					}
				}
				
				method.setEntity(builder.setCharset(CharsetUtils.get("UTF-8")).build());
			}
			
			response = httpClient.execute(method);
			
			
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				responseText = EntityUtils.toString(entity, "UTF-8");
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new DefinedError.ThirdErrorException("向第三方请求失败", e.getMessage());
		}

		return responseText;
	}
	
	public static String doJsonPost(String url, Map<String, Object> params) throws DefinedError {
		String responseText = null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		
		try {
			
	        HttpPost httpPost = new HttpPost(url);
	        
	        httpPost.addHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8");
	        
	        JSONObject json = new JSONObject();
	        if(null != params && params.size() > 0) {
	        	
	        	Iterator<Entry<String, Object>> it = params.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, Object> entry = it.next();
					json.put(entry.getKey(), entry.getValue());
				}
	        }
	        
	        StringEntity stringEntity  = new StringEntity(json.toJSONString(), "UTF-8");
	        stringEntity .setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));
	        
	        httpPost.setEntity(stringEntity);
	        response = httpClient.execute(httpPost);
	        
	        HttpEntity entity = response.getEntity();
			if (entity != null) {
				responseText = EntityUtils.toString(entity, "UTF-8");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new DefinedError.ThirdErrorException("向第三方请求失败", e.getMessage());
		}
		
		return responseText;
    }

	public static String doHttpsPost(String url,Map<String,String> map,String charset){
		CloseableHttpClient httpClient = null;
		HttpPost httpPost = null;
		String result = null;
		try {
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
				//信任所有
				public boolean isTrusted(X509Certificate[] chain,
										 String authType) throws CertificateException {
					return true;
				}
			}).build();
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
			httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
			httpPost = new HttpPost(url);
			if(map != null) {
				//设置参数
				List<NameValuePair> list = new ArrayList<NameValuePair>();
				Iterator iterator = map.entrySet().iterator();
				while(iterator.hasNext()){
					Entry<String,String> elem = (Entry<String, String>) iterator.next();
					list.add(new BasicNameValuePair(elem.getKey(),elem.getValue()));
				}
				if(list.size() > 0){
					UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list,charset);
					httpPost.setEntity(entity);
				}
			}
			HttpResponse response = httpClient.execute(httpPost);
			if(response != null){
				HttpEntity resEntity = response.getEntity();
				if(resEntity != null){
					result = EntityUtils.toString(resEntity,charset);
				}
			}
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

}
