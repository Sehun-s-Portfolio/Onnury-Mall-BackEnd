//package com.onnury.common.network;
//
//
//import com.onnury.common.util.LogUtil;
//import com.onnury.common.vo.DataMapVO;
//import lombok.Getter;
//import lombok.Setter;
//import lombok.ToString;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import sun.net.www.http.HttpClient;
////import org.apache.http.*;
////import org.apache.http.client.CookieStore;
////import org.apache.http.client.HttpClient;
////import org.apache.http.client.config.RequestConfig;
////import org.apache.http.client.config.RequestConfig.Builder;
////import org.apache.http.client.entity.UrlEncodedFormEntity;
////import org.apache.http.client.methods.CloseableHttpResponse;
////import org.apache.http.client.methods.HttpGet;
////import org.apache.http.client.methods.HttpPost;
////import org.apache.http.client.methods.HttpRequestBase;
////import org.apache.http.client.utils.URIBuilder;
////import org.apache.http.config.Registry;
////import org.apache.http.config.RegistryBuilder;
////import org.apache.http.conn.socket.ConnectionSocketFactory;
////import org.apache.http.conn.socket.PlainConnectionSocketFactory;
////import org.apache.http.conn.ssl.NoopHostnameVerifier;
////import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
////import org.apache.http.entity.ContentType;
////import org.apache.http.entity.StringEntity;
////import org.apache.http.entity.mime.HttpMultipartMode;
////import org.apache.http.entity.mime.MultipartEntityBuilder;
////import org.apache.http.impl.client.HttpClients;
////import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
////import org.apache.http.message.BasicNameValuePair;
////import org.apache.http.ssl.SSLContextBuilder;
////import org.apache.http.util.EntityUtils;
//
//import javax.net.ssl.SSLContext;
//import java.io.File;
//import java.net.CookieStore;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.Map;
//
//@Slf4j
//@Getter
//@Setter
//@ToString
//public class Http {
//	private static final Integer MAX_CONNECTIONS_PER_ROUTE = Integer.valueOf("100");
//	private static final Integer MAX_CONNECTIONS_TOTAL = Integer.valueOf("500");
//	private Integer connectionTimeout = Integer.valueOf("3000"); // default 3 seconds.
//	private Integer connectionRequestTimeout = Integer.valueOf("3000"); // default 3 seconds.
//	private Integer requestTimeout = Integer.valueOf("60000"); // default 60 seconds.
//
//	public static final String HEADER_NAME_LOCATION = "Location";
//	public static final String HEADER_NAME_COOKIE_REQ = "Cookie";
//	public static final String HEADER_NAME_COOKIE_RESP = "Set-Cookie";
//
//	public static final String METHOD_POST = "POST";
//	public static final String METHOD_GET = "GET";
//	public static final int STATUS_CODE_200 = HttpStatus.SC_OK;
//	public static final int STATUS_CODE_302 = HttpStatus.SC_MOVED_TEMPORARILY;
//
//	private static PoolingHttpClientConnectionManager connectionManager = null;
//	private HttpClient httpClient = null;
//	private Integer lastStatusCode = null;
//	private DataMapVO lastRespHeaders = null;
//	private String throwExceptionYn = "Y";
//	private static final Object LOCK_OBJ = new Object();
//
//	protected void processRespHeaders(final HttpResponse httpResp) {
//		Header[] respHeaders = httpResp.getAllHeaders();
//		if(respHeaders == null) {
//			log.error("processRespHeaders: no header.");
//			return;
//		}
//
//		DataMapVO respHeaderVO = new DataMapVO();
//		Header respHeader = null;
//		String headerName = null;
//
//		log.debug("[Http Respons Header Start]===============");
//        for (Header header : respHeaders) {
//            respHeader = header;
//            headerName = respHeader.getName();
//
//            log.debug("Header {} : {}", headerName, respHeader.getValue());
//
//            if (headerName.equals(HEADER_NAME_COOKIE_RESP)) {
//                ArrayList<String> cookies = null;
//                if (respHeaderVO.containsKey(headerName.toLowerCase())) {
//                    cookies = (ArrayList<String>) respHeaderVO.get(headerName.toLowerCase());
//                } else {
//                    cookies = new ArrayList<String>();
//                    respHeaderVO.put(headerName.toLowerCase(), cookies);
//                }
//
//                cookies.add(respHeader.getValue());
//            } else {
//                respHeaderVO.put(headerName.toLowerCase(), respHeader.getValue());
//            }
//        }
//
//		log.debug("[Http Respons Header End]===============");
//
//		if(respHeaderVO.isEmpty()) {
//			this.setLastRespHeaders(null);
//			return;
//		}
//
//		this.setLastRespHeaders(respHeaderVO);
//	}
//
//	protected HttpClient getHttpClient() {
//		return this.getHttpClient(null);
//	}
//
//	protected HttpClient getHttpClient(final CookieStore cookieStore) {
//		synchronized(LOCK_OBJ) {
//			HttpClient httpClient = this.httpClient;
//			if(httpClient == null) {
//				PoolingHttpClientConnectionManager cm = connectionManager;
//				if(cm == null) {
//					log.debug("[Http MaxConnectonRoute] : {}", MAX_CONNECTIONS_PER_ROUTE);
//					log.debug("[Http MaxConnecton] : {}", MAX_CONNECTIONS_TOTAL);
//
//					SSLContext sslContext = null;
//					Registry<ConnectionSocketFactory> registry = null;
//
//					try {
//						sslContext = new SSLContextBuilder()
//								.loadTrustMaterial(null, (x509CertChain, authType)->true)
//								.build();
//
//						registry = RegistryBuilder.<ConnectionSocketFactory>create()
//								.register("http", PlainConnectionSocketFactory.INSTANCE)
//								.register("Https", new SSLConnectionSocketFactory(sslContext, new String[] {"TLSv1", "TLSv1.1", "TLSv1.2"}, null, NoopHostnameVerifier.INSTANCE))
//								.build();
//					} catch ( Exception e) {
//						LogUtil.logException(e);
//					}
//
//					if(registry == null) {
//						cm = new PoolingHttpClientConnectionManager();
//					} else {
//						cm = new  PoolingHttpClientConnectionManager(registry);
//					}
//
//					cm.setDefaultMaxPerRoute(MAX_CONNECTIONS_PER_ROUTE);
//					cm.setMaxTotal(MAX_CONNECTIONS_TOTAL);
//					Http.connectionManager = cm;
//				}
//
//				if(cookieStore == null) {
//					httpClient = HttpClients.custom().setConnectionManager(cm).build();
//				} else {
//					httpClient = HttpClients.custom().setConnectionManager(cm).setDefaultCookieStore(cookieStore).build();
//				}
//
//				this.httpClient = httpClient;
//			}
//		}
//
//		return httpClient;
//	}
//
//	protected void close() {
//		if(this.httpClient == null ) {
//			return;
//		}
//	}
//
//	// method: METHOD_POST, METHOD_GET
//	// url: 호출 URL
//	// headers: http 헤더
//	// params: http 파라미터
//	public String request(final String method, final String url, final Map<String, String> headers,
//			final Map<String, String> params) throws Exception {
//		String respString = null;
//		HttpEntity respEntity = null;
//		HttpResponse httpResp = null;
//		int errLv = 0;
//
//		long startTime = System.nanoTime();
//		long estimateBeforeSending = 0;
//		long estimateAfterSending = 0;
//		long estimateFinishing = 0; // Estimate execution time.
//
//		/////
//		// method 설정
//		try {
//			HttpRequestBase httpMethod = null;
//			if(METHOD_GET.equals(method)) {
//				URIBuilder builder = new URIBuilder(url);
//
//				if(params != null) {
//					for(final String key : params.keySet()) {
//						builder.setParameter(key,  params.get(key));
//					}
//				}
//
//				httpMethod = new HttpGet(builder.build());
//			} else {
//				HttpPost post = new HttpPost(url);
//
//				if(params != null) {
//					ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
//
//					for(final String key : params.keySet() ) {
//						paramList.add(new BasicNameValuePair(key, params.get(key)));
//					}
//
//					post.setEntity(new UrlEncodedFormEntity(paramList, "UTF-8"));
//				}
//
//				httpMethod = post;
//			}
//
//			/////
//			// Http 타임아웃 설정
//			Integer connectionTimeout = this.getConnectionTimeout();
//			Integer connectionRequestTimeout = this.getConnectionRequestTimeout();
//			Integer requestTimeout = this.getRequestTimeout();
//			if( requestTimeout != null || connectionRequestTimeout != null || connectionTimeout != null) {
//				Builder cb = RequestConfig.custom();
//				if(connectionTimeout != null) {
//					cb = cb.setConnectTimeout(connectionTimeout);
//				}
//
//				if(connectionRequestTimeout != null) {
//					cb = cb.setConnectionRequestTimeout(connectionRequestTimeout);
//				}
//
//				if(requestTimeout != null) {
//					cb = cb.setSocketTimeout(requestTimeout);
//				}
//
//				RequestConfig config = cb.build();
//				httpMethod.setConfig(config);
//
//				/*
//				if(log.isInfoEnabled()) {
//					StringBuilder logSb = new StringBuilder();
//					logSb.append("\nHttp Config Start ===============");
//					logSb.append("\n[Connection Timeout]: {}");
//					logSb.append("\n[Connection Request Timeout]: {}");
//					logSb.append("\n[Request Timeout]: {}");
//					logSb.append("\nHttp Config End ===============");
//
//					log.info(logSb.toString(),
//							connectionTimeout,
//							connectionRequestTimeout,
//							requestTimeout);
//				}
//				 */
//			}
//
//			/////
//			// 헤더 설정
//			boolean isSetCharset = false;
//			if(headers != null) {
//				for(final String key : headers.keySet()) {
//					if("Accept-Charset".equalsIgnoreCase(key)) {
//						isSetCharset = true;
//					}
//
//					httpMethod.setHeader(key, headers.get(key));
//				}
//			}
//
//			if(!isSetCharset) {
//				httpMethod.setHeader("Accept-Charset", "UTF-8");
//			}
//
//			/////
//			// Http 통신
//			HttpClient httpClient = this.getHttpClient();
//			this.setLastStatusCode(null);
//
//			estimateBeforeSending = System.nanoTime();
//			httpResp = httpClient.execute(httpMethod);
//			estimateAfterSending = System.nanoTime();
//
//			errLv = 1;
//			int code = httpResp.getStatusLine().getStatusCode();
//			this.setLastStatusCode(code);
//			this.processRespHeaders(httpResp);
//
//			respEntity = httpResp.getEntity();
//			respString = EntityUtils.toString(respEntity);
//
//			if(code != HttpStatus.SC_OK) {
//				errLv = 2;
//				throw new Exception("[" + code + "] " + respString);
//			}
//
//			estimateFinishing = System.nanoTime();
//		} catch(Exception e ) {
//			String throwExceptionYn = this.getThrowExceptionYn();
//			if("Y".equals(throwExceptionYn)) {
//				throw e;
//			}
//
//			DataMapVO respVO = new DataMapVO();
//			respVO.put("rsltYn",  "Y");
//			respVO.put("rsltErrCode",  "L-" + errLv);
//			respVO.put("rsltMsg", "[" + e.getClass().getSimpleName() + "] " + e.getMessage());
//			respVO.put("exception", e.getClass().getSimpleName());
//			respString = VoUtil.toJson(respVO);
//
//			estimateFinishing = System.nanoTime();
//		} finally {
//			this.close();
//
//			if(respEntity != null) {
//				try {
//					EntityUtils.consumeQuietly(respEntity);
//				} catch (Exception e) {
//					LogUtil.logException(e);
//				}
//			}
//
//			if(httpResp instanceof CloseableHttpResponse) {
//				try {
//					((CloseableHttpResponse) httpResp).close();
//				} catch (Exception e) {
//					LogUtil.logException(e);
//				}
//			}
//
//			/*
//			try {
//				StringBuilder logSb = new StringBuilder();
//				logSb.append("\nHttp Start [{}]");
//				logSb.append("\n[Parameter]\n{}");
//				logSb.append("\n[Commuication Time]: [{}] millis");
//				logSb.append("\n[Total Time]: [{}] millis");
//				logSb.append("\nHttp End ===============");
//
//				log.info(logSb.toString(),
//						url,
//						VoUtil.StrMapToJson(params),
//						TimeUnit.NANOSECONDS.toMillis(estimateAfterSending - estimateBeforeSending),
//						TimeUnit.NANOSECONDS.toMillis(estimateFinishing - startTime));
//			} catch(Exception e) {
//				LogUtil.logException(e);
//			}
//			 */
//		}
//
//		return respString;
//	}
//
//	// url: 호출 URL
//	// headers: http 헤더
//	// params: http 파라미터
//	// fileParams: File 파라미터
//	public String request(final String url, final Map<String, String> headers,
//			final Map<String, String> params, final Map<String, File> fileParams) throws Exception {
//		String respString = null;
//		HttpEntity respEntity = null;
//		HttpResponse httpResp = null;
//		int errLv = 0;
//
//		long startTime = System.nanoTime();
//		long estimateBeforeSending = 0;
//		long estimateAfterSending = 0;
//		long estimateFinishing = 0; // Estimate execution time.
//
//		try {
//			HttpRequestBase httpMethod = null;
//			HttpPost post = new HttpPost(url);
//
//			MultipartEntityBuilder meb = MultipartEntityBuilder.create();
//			meb.setCharset(StandardCharsets.UTF_8);
//			meb.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//
//			///// 파일 탑재
//			File file = null;
//			for(final String key : fileParams.keySet()) {
//				file = fileParams.get(key);
//				if(file == null) {
//					continue;
//				}
//
//				meb.addBinaryBody(key,  file);
//			}
//
//			///// 매개변수
//			String value = null;
//			for(final String key : params.keySet()) {
//				value = params.get(key);
//				if(value == null) {
//					continue;
//				}
//
//				meb.addTextBody(key,  value, ContentType.MULTIPART_FORM_DATA.withCharset("UTF-8"));
//			}
//
//			HttpEntity entity = meb.build();
//			post.setEntity(entity);
//
//			httpMethod = post;
//
//			///// Request Timeout 설정
//			Integer connectionTimeout = this.getConnectionTimeout();
//			Integer connectionRequestTimeout = this.getConnectionRequestTimeout();
//			Integer requestTimeout = this.getRequestTimeout();
//			if( requestTimeout != null || connectionRequestTimeout != null || connectionTimeout != null) {
//				Builder cb = RequestConfig.custom();
//				if(connectionTimeout != null) {
//					cb = cb.setConnectTimeout(connectionTimeout);
//				}
//
//				if(connectionRequestTimeout != null) {
//					cb = cb.setConnectionRequestTimeout(connectionRequestTimeout);
//				}
//
//				if(requestTimeout != null) {
//					cb = cb.setSocketTimeout(requestTimeout);
//				}
//
//				RequestConfig config = cb.build();
//				httpMethod.setConfig(config);
//
//				/*
//				if(log.isInfoEnabled()) {
//					StringBuilder logSb = new StringBuilder();
//					logSb.append("\nHttp Config Start ===============");
//					logSb.append("\n[Connection Timeout]: {}");
//					logSb.append("\n[Connection Request Timeout]: {}");
//					logSb.append("\n[Request Timeout]: {}");
//					logSb.append("\nHttp Config End ===============");
//
//					log.info(logSb.toString(),
//							connectionTimeout,
//							connectionRequestTimeout,
//							requestTimeout);
//				}
//				*/
//			}
//
//			boolean isSetCharset = false;
//			if(headers != null) {
//				for(final String key : headers.keySet()) {
//					if("Accept-Charset".equalsIgnoreCase(key)) {
//						isSetCharset = true;
//					}
//
//					httpMethod.setHeader(key, headers.get(key));
//				}
//			}
//
//			if(!isSetCharset) {
//				httpMethod.setHeader("Accept-Charset", "UTF-8");
//			}
//
//			/////
//			// Http 통신
//			HttpClient httpClient = this.getHttpClient();
//			this.setLastStatusCode(null);
//
//			estimateBeforeSending = System.nanoTime();
//			httpResp = httpClient.execute(httpMethod);
//			estimateAfterSending = System.nanoTime();
//
//			errLv = 1;
//			int code = httpResp.getStatusLine().getStatusCode();
//			this.setLastStatusCode(code);
//			this.processRespHeaders(httpResp);
//
//			respEntity = httpResp.getEntity();
//			respString = EntityUtils.toString(respEntity);
//
//			if(code != HttpStatus.SC_OK) {
//				errLv = 2;
//				throw new Exception("[" + code + "] " + respString);
//			}
//
//			estimateFinishing = System.nanoTime();
//		} catch(Exception e ) {
//			String throwExceptionYn = this.getThrowExceptionYn();
//			if("Y".equals(throwExceptionYn)) {
//				throw e;
//			}
//
//			DataMapVO respVO = new DataMapVO();
//			respVO.put("rsltYn",  "Y");
//			respVO.put("rsltErrCode",  "L-" + errLv);
//			respVO.put("rsltMsg", "[" + e.getClass().getSimpleName() + "] " + e.getMessage());
//			respVO.put("exception", e.getClass().getSimpleName());
//			respString = VoUtil.toJson(respVO);
//
//			estimateFinishing = System.nanoTime();
//		} finally {
//			this.close();
//
//			if(respEntity != null) {
//				try {
//					EntityUtils.consumeQuietly(respEntity);
//				} catch (Exception e) {
//					LogUtil.logException(e);
//				}
//			}
//
//			if(httpResp instanceof CloseableHttpResponse) {
//				try {
//					((CloseableHttpResponse) httpResp).close();
//				} catch (Exception e) {
//					LogUtil.logException(e);
//				}
//			}
//
//			/*
//			try {
//				StringBuilder logSb = new StringBuilder();
//				logSb.append("\nHttp Start [{}] ===============");
//				logSb.append("\n[Commuication Time]: [{}] millis");
//				logSb.append("\n[Total Time]: [{}] millis");
//				logSb.append("\nHttp End ===============");
//
//				log.info(logSb.toString(),
//						url,
//						TimeUnit.NANOSECONDS.toMillis(estimateAfterSending - estimateBeforeSending),
//						TimeUnit.NANOSECONDS.toMillis(estimateFinishing - startTime));
//
//			} catch(Exception e) {
//				LogUtil.logException(e);
//			}
//			 */
//		}
//
//		return respString;
//	}
//
//	// url: 호출 URL
//	// headers: http 헤더
//	// params: http 파라미터
//	// strContent: XML 데이터
//	// strContentType: "text/xml"
//	public String request(final String url, final Map<String, String> headers,
//						  final String strContent, final String strContentType) throws Exception {
//		String respString = null;
//		HttpEntity respEntity = null;
//		HttpResponse httpResp = null;
//		int errLv = 0;
//
//		long startTime = System.nanoTime();
//		long estimateBeforeSending = 0;
//		long estimateAfterSending = 0;
//		long estimateFinishing = 0; // Estimate execution time.
//
//		try {
//			if(StringUtil.isNull(strContent)) {
//				throw new Exception("전송할 XML 데이터가 없습니다.");
//			}
//
//			HttpPost httpMethod = new HttpPost(url);
//
//			// Http 타임아웃 설정
//			Integer connectionTimeout = this.getConnectionTimeout();
//			Integer connectionRequestTimeout = this.getConnectionRequestTimeout();
//			Integer requestTimeout = this.getRequestTimeout();
//			if( requestTimeout != null || connectionRequestTimeout != null || connectionTimeout != null) {
//				Builder cb = RequestConfig.custom();
//				if(connectionTimeout != null) {
//					cb = cb.setConnectTimeout(connectionTimeout);
//				}
//
//				if(connectionRequestTimeout != null) {
//					cb = cb.setConnectionRequestTimeout(connectionRequestTimeout);
//				}
//
//				if(requestTimeout != null) {
//					cb = cb.setSocketTimeout(requestTimeout);
//				}
//
//				RequestConfig config = cb.build();
//				httpMethod.setConfig(config);
//
//				/*
//				if(log.isInfoEnabled()) {
//					StringBuilder logSb = new StringBuilder();
//					logSb.append("\nHttp Config Start ===============");
//					logSb.append("\n[Connection Timeout]: {}");
//					logSb.append("\n[Connection Request Timeout]: {}");
//					logSb.append("\n[Request Timeout]: {}");
//					logSb.append("\nHttp Config End ===============");
//
//					log.info(logSb.toString(),
//							connectionTimeout,
//							connectionRequestTimeout,
//							requestTimeout);
//				}
//				 */
//			}
//
//			if(headers != null) {
//				for(final String key : headers.keySet()) {
//					httpMethod.setHeader(key, headers.get(key));
//				}
//			}
//
//			StringEntity entity = null;
//			if(strContentType == null) {
//				entity = new StringEntity(strContent, Consts.UTF_8);
//			} else {
//				entity = new StringEntity(strContent, ContentType.create(strContentType, Consts.UTF_8));
//			}
//
//			entity.setChunked(true);
//			httpMethod.setEntity(entity);
//
//			/////
//			// Http 통신
//			HttpClient httpClient = this.getHttpClient();
//			this.setLastStatusCode(null);
//
//			estimateBeforeSending = System.nanoTime();
//			httpResp = httpClient.execute(httpMethod);
//			estimateAfterSending = System.nanoTime();
//
//			errLv = 1;
//			int code = httpResp.getStatusLine().getStatusCode();
//			this.setLastStatusCode(code);
//			this.processRespHeaders(httpResp);
//
//			respEntity = httpResp.getEntity();
//			log.debug("HTTP request: ContentType[{}], ContentEncoding[{}]",
//					respEntity.getContentType(), respEntity.getContentEncoding());
//			respString = EntityUtils.toString(respEntity, "UTF-8");
//
//			if(code != HttpStatus.SC_OK) {
//				errLv = 2;
//				throw new Exception("[" + code + "] " + respString);
//			}
//
//			estimateFinishing = System.nanoTime();
//		} catch(Exception e ) {
//			String throwExceptionYn = this.getThrowExceptionYn();
//			if("Y".equals(throwExceptionYn)) {
//				throw e;
//			}
//
//			DataMapVO respVO = new DataMapVO();
//			respVO.put("rsltYn",  "Y");
//			respVO.put("rsltErrCode",  "L-" + errLv);
//			respVO.put("rsltMsg", "[" + e.getClass().getSimpleName() + "] " + e.getMessage());
//			respVO.put("exception", e.getClass().getSimpleName());
//			respString = VoUtil.toJson(respVO);
//
//			estimateFinishing = System.nanoTime();
//		} finally {
//			this.close();
//
//			if(respEntity != null) {
//				try {
//					EntityUtils.consumeQuietly(respEntity);
//				} catch (Exception e) {
//					LogUtil.logException(e);
//				}
//			}
//
//			if(httpResp instanceof CloseableHttpResponse) {
//				try {
//					((CloseableHttpResponse) httpResp).close();
//				} catch (Exception e) {
//					LogUtil.logException(e);
//				}
//			}
//
//			/*
//			try {
//				StringBuilder logSb = new StringBuilder();
//				logSb.append("\nHttp Start [{}] ===============");
//				logSb.append("\n[Commuication Time]: [{}] millis");
//				logSb.append("\n[Total Time]: [{}] millis");
//				logSb.append("\nHttp End ===============");
//
//				log.info(logSb.toString(),
//						url,
//						TimeUnit.NANOSECONDS.toMillis(estimateAfterSending - estimateBeforeSending),
//						TimeUnit.NANOSECONDS.toMillis(estimateFinishing - startTime));
//			} catch(Exception e) {
//				LogUtil.logException(e);
//			}
//			 */
//		}
//
//		return respString;
//	}
//
//	// url: 호출 URL
//	// params: http 파라미터
//	public String request(final String url, final Map<String, String> params) throws Exception {
//		return this.request(METHOD_POST, url, null, params);
//	}
//
//	// url: 호출 URL
//	// headers: http 헤더
//	// strXml: xml 데이터
//	public String request(final String url, final Map<String, String> headers, final String strXml) throws Exception {
//		return request(url, headers, strXml, "text/xml");
//	}
//
//	// url: 호출 URL
//	public String sendGet(final String url) throws Exception {
//		String respString = null;
//		HttpEntity respEntity = null;
//		HttpResponse httpResp = null;
//
//		try {
//			HttpClient httpClient = this.getHttpClient();
//			HttpGet httpGet = new HttpGet(url);
//
//			httpGet.addHeader("User-Agent", "Mozila/5.0");
//			httpResp = httpClient.execute(httpGet);
//
//			respEntity = httpResp.getEntity();
//
//			log.debug("request: ContentType[{}], ContentEncoding[{}]", respEntity.getContentType(), respEntity.getContentEncoding());
//
//			if(respEntity != null) {
//				respString = EntityUtils.toString(respEntity, "UTF-8");
//			}
//		} finally {
//			this.close();
//
//			if(respEntity != null) {
//				try {
//					EntityUtils.consumeQuietly(respEntity);
//				} catch (Exception e) {
//					LogUtil.logException(e);
//				}
//			}
//
//			if(httpResp instanceof CloseableHttpResponse) {
//				try {
//					((CloseableHttpResponse) httpResp).close();
//				} catch (Exception e) {
//					LogUtil.logException(e);
//				}
//			}
//		}
//
//		return respString;
//	}
//
//
//
//}