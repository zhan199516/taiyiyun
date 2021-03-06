package com.taiyiyun.passport.yunsign;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;

public class HttpConnectionManager {
	static DefaultHttpClient httpClient = null;

	public static void init() throws KeyManagementException, KeyStoreException, NoSuchAlgorithmException {
		httpClient = new DefaultHttpClient();
		SSLContext ctx = SSLContext.getInstance("TLS");

		ctx.init(null, new TrustManager[] { tm }, null);
		SSLSocketFactory ssf = new SSLSocketFactory(ctx,
				SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		ClientConnectionManager ccm = httpClient.getConnectionManager();
		SchemeRegistry sr = ccm.getSchemeRegistry();
		sr.register(new Scheme("https", 443, ssf));
		
		HttpParams params = new BasicHttpParams();

		//设置连接超时时间

		Integer CONNECTION_TIMEOUT = 5 * 1000; //设置请求超时2秒钟 根据业务调整 

		Integer SO_TIMEOUT = 5 * 1000;  //设置等待数据超时时间2秒钟 根据业务调整 

		//定义了当从ClientConnectionManager中检索ManagedClientConnection实例时使用的毫秒级的超时时间

		//这个参数期望得到一个java.lang.Long类型的值。如果这个参数没有被设置，默认等于CONNECTION_TIMEOUT，因此一定要设置

		Long CONN_MANAGER_TIMEOUT = 500L;  //该值就是连接不够用的时候等待超时时间，一定要设置，而且不能太大 () 
		params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTION_TIMEOUT);

		params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, SO_TIMEOUT);

		params.setLongParameter(ClientPNames.CONN_MANAGER_TIMEOUT, CONN_MANAGER_TIMEOUT);

		//在提交请求之前 测试连接是否可用 
		params.setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, true);	 

		PoolingClientConnectionManager conMgr = new PoolingClientConnectionManager();

		conMgr.setMaxTotal(5000); //设置整个连接池最大连接数 根据自己的场景决定 

		//是路由的默认最大连接（该值默认为2），限制数量实际使用DefaultMaxPerRoute并非MaxTotal。

		//设置过小无法支持大并发(ConnectionPoolTimeoutException: Timeout waiting for connection from pool)，路由是对maxTotal的细分。

		conMgr.setDefaultMaxPerRoute(conMgr.getMaxTotal());//（目前只有一个路由，因此让他等于最大值）

		//另外设置http client的重试次数，默认是3次；当前是禁用掉（如果项目量不到，这个默认即可）
		httpClient.setParams(params);
		httpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
	}

	public static DefaultHttpClient getHttpClient() {
		if (null == httpClient) {
			try {
				init();
			} catch (KeyManagementException | KeyStoreException
					| NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/*
		 * CloseableHttpClient httpClient =
		 * HttpClients.createDefault();//如果不采用连接池就是这种方式获取连接
		 */
		return httpClient;
	}

	static X509TrustManager tm = new X509TrustManager() {
		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	};

}
