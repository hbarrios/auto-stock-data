/**
 * Copyright (c) 2011 TLOSS. All rights reserved.
 * Created on Sep 1, 2011
 * File name : ZingDeal.java
 * Package org.tloss.zingdeal
 */
package org.tloss.find1deal;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HTTP;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;
import org.tloss.common.Article;
import org.tloss.common.DefaultResponseHandler;
import org.tloss.multipos.PostArticle;

/**
 * @author tungt
 * 
 */
public class Find1Deal {
	DefaultHttpClient httpclient = new DefaultHttpClient();
	ResponseHandler<String> responseHandler = new DefaultResponseHandler();

	public void setHeader(AbstractHttpMessage http) {
		http.setHeader(
				"User-Agent",
				"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2.17) Gecko/20110420 Firefox/3.6.17");
		http.setHeader("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		http.setHeader("Accept-Language", "en-gb,en;q=0.5");
		http.setHeader("Accept-Encoding", "gzip,deflate");
		http.setHeader("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
		http.setHeader("Connection", "keep-alive");
		http.setHeader("Keep-Alive", "115");
		// http.setHeader("Referer", "http://www.ddth.com/forum.php");
	}

	public void initHttpClient(HttpClient httpclient) {
		httpclient.getParams().setParameter(
				CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1); // Default
																			// to
																			// HTTP
																			// 1.0
		httpclient.getParams().setParameter(
				CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");
		httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
				CookiePolicy.BROWSER_COMPATIBILITY);
	}

	/**
	 * buoc 1 : <br/>
	 * truy cap vao http://haiphongit.com/forum/index.php<br/>
	 * Lay thong tin securitytoken trong the input<br/>
	 * <input type="hidden" name="securitytoken"
	 * value="1308967949-a4c8fcec93507a24dc933e6878c9da0afb2347af" /><br/>
	 * Buoc 2: ma hoa mat khau voi md5<br/>
	 * "http://haiphongit.com/forum/login.php?do=login"<br/>
	 * vb_login_username =username<br/>
	 * vb_login_password<br/>
	 * s<br/>
	 * securitytoken =noi dung securitytoken<br/>
	 * do =login<br/>
	 * vb_login_md5password =mat khau ma hoa<br/>
	 * vb_login_md5password_utf =HaiPhongITPost<br/>
	 * Buoc3: kiem tra dang nhap thanh cong va chuyen ve trang chu<br/>
	 * http://haiphongit.com/forum/index.php<br/>
	 * 
	 * @throws Exception
	 */
	public boolean login(String username, String password) throws Exception {
		return login(username, password, false, null);
	}

	public String getUrl(int type, Object[] options) {
		String url = "";
		switch (type) {
		case PostArticle.LOGIN_FORM_URL:
			url = "http://51deal.vn/account/login.php";
			break;
		case PostArticle.LOGIN_POST_URL:
			url = "http://51deal.vn/account/login.php";
			break;
		case PostArticle.POST_FORM_URL:
			if (options != null && options.length > 0) {
				url = "http://www.ddth.com/newthread.php?do=newthread&f="

				+ options[0];
			}
			break;
		case PostArticle.POST_URL:
			if (options != null && options.length > 0) {
				url = "http://www.ddth.com/newthread.php?do=postthread&f="
						+ options[0];
			}
			break;
		}
		return url;
	}

	/**
	 * buoc 0: vao link efit<br/>
	 * http://haiphongit.com/forum/newthread.php?do=newthread&f=90<br/>
	 * buoc 1: gui bai<br/>
	 * http://haiphongit.com/forum/newthread.php?do=postthread&f=90<br/>
	 * subject tieu de <br/>
	 * threaddesc mo ta <br/>
	 * message nodung <br/>
	 * wysiwyg 0<br/>
	 * iconid 0<br/>
	 * s <br/>
	 * securitytoken so ma sercurity<br/>
	 * f chuyen muc. vi du 90<br/>
	 * do postthread<br/>
	 * posthash <br/>
	 * poststarttime<br/>
	 * loggedinuser 195249<br/>
	 * sbutton Gởi Ðề Tài Mới<br/>
	 * parseurl 1<br/>
	 * emailupdate 9999<br/>
	 * polloptions 4<br/>
	 * 
	 * buoc2: rediect den bai viet <br/>
	 * http://haiphongit.com/forum/showthread.php?p=207598#post207598
	 * 
	 * 
	 */
	public boolean post(Article article, String urlEdit, String urlPost,
			Object[] options) throws Exception {
		boolean result = false;
		initHttpClient(httpclient);

		HttpGet httpGetStepOne = new HttpGet(urlEdit);
		setHeader(httpGetStepOne);
		String responseBody = httpclient.execute(httpGetStepOne,
				responseHandler);
		// responseBody =responseBody.replaceAll("&quot;TÃ¡m&quot;", "Tam");
		responseBody = responseBody.replaceAll("\"TÃ¡m\"", "Tam");

		// System.out.println(responseBody);
		// lay ra noi dung xml
		CleanerProperties props = new CleanerProperties();
		// set some properties to non-default values
		props.setTranslateSpecialEntities(true);
		props.setTransResCharsToNCR(true);
		props.setOmitComments(true);
		// do parsing
		TagNode tagNode = new HtmlCleaner(props).clean(new StringReader(
				responseBody));
		// serialize to xml file
		String xml = new PrettyXmlSerializer(props).getAsString(tagNode,
				"utf-8");
		// System.out.println(xml);
		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(xml));
		// <input type="hidden" value="0" id="vB_Editor_001_mode"
		// name="wysiwyg">
		List<?> list = document.selectNodes("//div[@class='MuaDeal']/a");
		String link = null;
		for (Object object : list) {
			Element element = (Element) object;
			if (element.attribute("href") != null) {
				if (link == null)
					link = element.attribute("href").getValue();
			}
		}
		if (!"#this".equals(link)) {
			httpGetStepOne = new HttpGet("http://deal.zing.vn" + link);
			setHeader(httpGetStepOne);
			responseBody = httpclient.execute(httpGetStepOne, responseHandler);
			tagNode = new HtmlCleaner(props).clean(new StringReader(
					responseBody));
			// serialize to xml file
			xml = new PrettyXmlSerializer(props).getAsString(tagNode, "utf-8");
			// System.out.println(xml);
			reader = new SAXReader();
			document = reader.read(new StringReader(xml));
			list = document.selectNodes("//form[@id='DealBuyForm']");
			for (Object object : list) {
				Element element = (Element) object;
				if (element.attribute("action") != null) {

					link = element.attribute("action").getValue();
				}
				HttpPost httpPost = new HttpPost("http://deal.zing.vn" + link);
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				List<?> list2 = element
						.selectNodes("//form[@id='DealBuyForm']//input");
				for (Object object2 : list2) {
					Element element2 = (Element) object2;
					if (element2.attribute("name") != null
							&& element2.attribute("value") != null) {
						if (element2.attribute("id") != null
								&& ("DealPaymentTypeIdTmp6".equals(element2
										.attribute("id").getValue()) || "DealPaymentTypeId6"
										.equals(element2.attribute("id")
												.getValue()))) {

						} else if ("data[Deal][user_id]".equals(element2
								.attribute("name").getValue())) {
							nvps.add(new BasicNameValuePair(element2.attribute(
									"name").getValue(), (String) options[0]));
							System.out.println(element2.attribute("name")
									.getValue() + " : " + (String) options[0]);
						} else {

							nvps.add(new BasicNameValuePair(element2.attribute(
									"name").getValue(), element2.attribute(
									"value").getValue()));
							System.out.println(element2.attribute("name")
									.getValue()
									+ " : "
									+ element2.attribute("value").getValue());
						}
					}

				}
				list2 = element
						.selectNodes("//form[@id='DealBuyForm']//select");
				for (Object object2 : list2) {
					Element element2 = (Element) object2;
					if (element2.attribute("name") != null) {
						Iterator<?> iterator = element2.nodeIterator();
						for (; iterator.hasNext();) {
							Node ele3 = (Node) iterator.next();
							if (ele3 instanceof Element) {
								Element element3 = (Element) ele3;
								if (element3.attribute("selected") != null
										&& "selected".equals(element3
												.attribute("selected")
												.getValue())
										&& element3.attribute("value") != null) {
									nvps.add(new BasicNameValuePair(element2
											.attribute("name").getValue(),
											element3.attribute("value")
													.getValue()));
									System.out.println(element2.attribute(
											"name").getValue()
											+ " : "
											+ element3.attribute("value")
													.getValue());
								}
							}
						}
					}

				}

				httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
				setHeader(httpPost);
				responseBody = httpclient.execute(httpPost, responseHandler);
				System.out.println(responseBody);
			}
			result = true;
		}

		return result;
	}

	public void logout() {

	}

	public boolean login(String username, String password,
			boolean encrytedPassword, Object[] options) throws Exception {
		boolean result = false;
		initHttpClient(httpclient);

		HttpGet httpGetStepOne = null;
		String responseBody = null;

		HttpPost httpPost = new HttpPost(getUrl(PostArticle.LOGIN_POST_URL,
				null));
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("email", username));
		nvps.add(new BasicNameValuePair("password", password));
		nvps.add(new BasicNameValuePair("askid", ""));

		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		setHeader(httpPost);
		responseBody = httpclient.execute(httpPost, responseHandler);
		// Redirect to:
		// http://51deal.vn/index.php
		httpGetStepOne = new HttpGet("http://51deal.vn/index.php");
		setHeader(httpGetStepOne);
		responseBody = httpclient.execute(httpGetStepOne, responseHandler);
		System.out.println(responseBody);
		result = responseBody.indexOf("users/logout") >= 0;
		BasicClientCookie cookie = new BasicClientCookie("notified", "1");
		cookie.setDomain("");
		cookie.setPath("/");
		cookie.setExpiryDate(new Date(new Date().getTime() + 1000));
		httpclient.getCookieStore().addCookie(cookie);
		httpGetStepOne = new HttpGet("http://51deal.vn/index.php");
		setHeader(httpGetStepOne);
		responseBody = httpclient.execute(httpGetStepOne, responseHandler);
		System.out.println(responseBody);
		return result;
	}

	public static void main(String[] args) throws Exception {

		Find1Deal article = new Find1Deal();
		boolean logined = false;
		while (!logined) {
			try {
				logined = article.login("phale0101856", "", true,
						new Object[] { "", "" });
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		boolean escape = false;
		while (true) {
			try {
				escape = article
						.post(null,
								"http://deal.zing.vn/tp-ho-chi-minh/deal/hot-cua-hot-chi-500d-de-so-huu-ngay-mot-cap-ve-tham-du-dem-gala-am-nhac-voi-su-gop-mat-tham-gia-cua-nhieu-ca-si",
								null, new Object[] { "446852" });
				System.out.println(escape);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}