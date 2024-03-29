/**
 * Copyright (c) 2011 TLOSS. All rights reserved.
 * Created on Sep 1, 2011
 * File name : FiveSeconds.java
 * Package org.tloss.fiveseconds
 */
package org.tloss.fiveseconds;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.cookie.Cookie;
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
import org.tloss.common.DefaultResponseAndFollowHandler;
import org.tloss.multipos.PostArticle;
import org.tloss.multipos.tinhtevn.TinhTeVNPost;

/**
 * @author tungt
 * 
 */
public class FiveSeconds implements PostArticle {

	public FiveSeconds() {

	}

	DefaultHttpClient httpclient = new DefaultHttpClient();
	DefaultResponseAndFollowHandler responseHandler = new DefaultResponseAndFollowHandler();

	public void setHeader(AbstractHttpMessage http) {
		http.setHeader(
				"User-Agent",
				"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2.17) Gecko/20110420 Firefox/3.6.17");
		http.setHeader("Accept", "text/html, */*");
		http.setHeader("Accept-Language", "en-gb,en;q=0.5");
		http.setHeader("Accept-Encoding", "gzip,deflate");
		http.setHeader("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
		http.setHeader("Keep-Alive", "115");
		http.setHeader("Connection", "keep-alive");
		http.setHeader("Pragma", "no-cache");
		http.setHeader("Cache-Control", "no-cache");
		http.setHeader("Host", "www.5giay.vn");

	}

	Invocable invocableEngine;

	public Invocable initScriptEngine(String data) {
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");
		try {
			jsEngine.eval(new StringReader(data));
			invocableEngine = (Invocable) jsEngine;
		} catch (ScriptException e) {
		}
		return invocableEngine;
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
		// HttpHost proxy = new HttpHost("172.16.203.1", 8080);
		// httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
		// proxy);
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
			url = "http://www.5giay.vn/";
			break;
		case PostArticle.LOGIN_POST_URL:
			url = "http://www.5giay.vn/login.php?do=login";
			break;
		case PostArticle.POST_FORM_URL:
			if (options != null && options.length > 0) {
				url = "http://www.tinhte.vn/newthread.php?do=newthread&f="

				+ options[0];
			}
			break;
		case PostArticle.POST_URL:
			if (options != null && options.length > 0) {
				url = "http://www.5giay.vn/newreply.php?do=postreply&t="
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
	 * sbutton Gá»Ÿi Ã�á»� TÃ i Má»›i<br/>
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
		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(xml));
		// <input type="hidden" value="0" id="vB_Editor_001_mode"
		// name="wysiwyg">
		List<?> list = document.selectNodes("//input[@name='wysiwyg']");
		String wysiwyg = null;
		for (Object object : list) {
			Element element = (Element) object;
			if (element.attribute("value") != null) {
				if (wysiwyg == null)
					wysiwyg = element.attribute("value").getValue();
			}
		}
		// <input type="radio" checked="checked" onclick="swap_posticon(null)"
		// tabindex="1" id="rb_iconid_0" value="0" name="iconid">
		list = document.selectNodes("//input[@name='iconid']");
		String iconid = "0";
		for (Object object : list) {
			Element element = (Element) object;
			if (element.attribute("value") != null) {
				if (iconid == null)
					iconid = element.attribute("value").getValue();

			}
		}
		// <input type="hidden" value="" name="s">
		list = document.selectNodes("//input[@name='s']");
		String s = null;
		for (Object object : list) {
			Element element = (Element) object;
			if (element.attribute("value") != null) {
				if (s == null)
					s = element.attribute("value").getValue();

			}
		}
		// <input type="hidden"
		// value="1309151722-901d4c8164fd820e30b2ff1e2e622db80d51f21a"
		// name="securitytoken">
		list = document.selectNodes("//input[@name='securitytoken']");
		String securitytoken = null;
		for (Object object : list) {
			Element element = (Element) object;
			if (element.attribute("value") != null) {
				if (securitytoken == null)
					securitytoken = element.attribute("value").getValue();

			}
		}
		// <input type="hidden" value="90" name="f">
		list = document.selectNodes("//input[@name='f']");
		String f = null;
		for (Object object : list) {
			Element element = (Element) object;
			if (element.attribute("value") != null) {
				if (f == null)
					f = element.attribute("value").getValue();

			}
		}
		// <input type="hidden" value="" name="posthash">
		list = document.selectNodes("//input[@name='posthash']");
		String posthash = null;
		for (Object object : list) {
			Element element = (Element) object;
			if (element.attribute("value") != null) {
				if (posthash == null)
					posthash = element.attribute("value").getValue();

			}
		}
		// <input type="hidden" value="" name="poststarttime">
		list = document.selectNodes("//input[@name='poststarttime']");
		String poststarttime = null;
		for (Object object : list) {
			Element element = (Element) object;
			if (element.attribute("value") != null) {
				if (poststarttime == null)
					poststarttime = element.attribute("value").getValue();

			}
		}
		// <input type="hidden" value="195249" name="loggedinuser">
		list = document.selectNodes("//input[@name='loggedinuser']");
		String loggedinuser = null;
		for (Object object : list) {
			Element element = (Element) object;
			if (element.attribute("value") != null) {
				if (loggedinuser == null)
					loggedinuser = element.attribute("value").getValue();

			}
		}
		HttpPost httpPost = new HttpPost(urlPost);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("subject", article.getTitle()));
		nvps.add(new BasicNameValuePair("threaddesc", ""));
		nvps.add(new BasicNameValuePair("message", article.getContent()));
		nvps.add(new BasicNameValuePair("wysiwyg", wysiwyg));
		nvps.add(new BasicNameValuePair("iconid", iconid));
		nvps.add(new BasicNameValuePair("s", s));
		nvps.add(new BasicNameValuePair("securitytoken", securitytoken));
		nvps.add(new BasicNameValuePair("f", f));
		nvps.add(new BasicNameValuePair("do", "postthread"));
		nvps.add(new BasicNameValuePair("posthash", posthash));
		nvps.add(new BasicNameValuePair("poststarttime", poststarttime));
		nvps.add(new BasicNameValuePair("loggedinuser", loggedinuser));
		nvps.add(new BasicNameValuePair("sbutton", "Gá»­i Chá»§ Ä‘á»�"));
		nvps.add(new BasicNameValuePair("parseurl", "1"));
		nvps.add(new BasicNameValuePair("emailupdate", "9999"));
		nvps.add(new BasicNameValuePair("polloptions", "4"));
		httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		setHeader(httpPost);
		responseBody = httpclient.execute(httpPost, responseHandler);
		return result;
	}

	public void logout() {

	}

	public String[] md5hash(String password) throws ScriptException,
			NoSuchMethodException {
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");
		jsEngine.eval(new InputStreamReader(TinhTeVNPost.class
				.getResourceAsStream("vbulletin_md5_v414.js")));
		Invocable invocableEngine = (Invocable) jsEngine;
		Object result = invocableEngine.invokeFunction("md5hash", password);
		List<?> arr = (List<?>) result;
		String[] array = new String[(int) arr.size()];
		int index = 0;
		for (Object o : arr) {
			array[index] = (String) o;
			index++;
		}

		return array;

	}

	public boolean login(String username, String password,
			boolean encrytedPassword, Object[] options) throws Exception {
		boolean result = false;
		initHttpClient(httpclient);
		String responseBody;
		// lay ra noi dung xml
		CleanerProperties props = new CleanerProperties();
		// set some properties to non-default values
		props.setTranslateSpecialEntities(true);
		props.setTransResCharsToNCR(true);
		props.setOmitComments(true);
		// do parsing
		TagNode tagNode;
		// serialize to xml file
		String xml;
		SAXReader reader = new SAXReader();
		Document document;
		// neu lay dc ma securitytoken
		String[] passwords;
		if (!encrytedPassword) {
			passwords = md5hash(password);
		} else {
			passwords = new String[2];
			if (options != null && options.length >= 2) {
				passwords[0] = (String) options[0];
				passwords[1] = (String) options[1];
			}
		}
		HttpPost httpPost = new HttpPost(getUrl(LOGIN_POST_URL, null));
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("vb_login_username", username));
		nvps.add(new BasicNameValuePair("cookieuser", "1"));
		nvps.add(new BasicNameValuePair("vb_login_password", ""));
		nvps.add(new BasicNameValuePair("s", ""));
		nvps.add(new BasicNameValuePair("securitytoken", "guest"));
		nvps.add(new BasicNameValuePair("do", "login"));
		nvps.add(new BasicNameValuePair("vb_login_md5password", passwords[0]));
		nvps.add(new BasicNameValuePair("vb_login_md5password_utf",
				passwords[1]));
		httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		setHeader(httpPost);
		responseBody = httpclient.execute(httpPost, responseHandler);

		if (responseBody != null
				&& responseBody.indexOf("CÃ¡m Æ¡n báº¡n Ä‘Ã£ Ä‘Äƒng nháº­p") > 0) {

			//
			tagNode = new HtmlCleaner(props).clean(new StringReader(
					responseBody));
			// serialize to xml file
			xml = new PrettyXmlSerializer(props).getAsString(tagNode, "utf-8");
			reader = new SAXReader();
			document = reader.read(new StringReader(xml));
			List list = document.selectNodes("//meta[@http-equiv='Refresh']");
			// content="2; URL=http://www.tinhte.vn/forum.php?s=d9056bfd27c00b24feeac3f8a7e14baa"
			String nextUrl = "";
			for (Object object : list) {
				Element element = (Element) object;
				if (element.attribute("content") != null) {
					nextUrl = element.attribute("content").getValue();
					if (nextUrl.indexOf("URL=") > 0) {
						nextUrl = nextUrl
								.substring(nextUrl.indexOf("URL=") + 4);
					}
				}
			}
			result = true;
			HttpGet htttGet = new HttpGet(nextUrl);
			setHeader(htttGet);
			responseBody = httpclient.execute(htttGet, responseHandler);
		} else if (responseBody != null) {
			int index1 = responseBody.indexOf("<script>function toNumbers");
			if (index1 >= 0) {
				int index2 = responseBody.indexOf("</script>", index1);
				if (index2 >= 0) {
					String javascript = responseBody.substring(index1 + 8,
							index2);
					int index3 = javascript.indexOf("document.cookie=");
					if (index3 >= 0) {
						String javascript2 = javascript.substring(0,index3);
						String javascript3 = javascript.substring(index3);
						String javascript4 = "function slowAES_decrypt(){ return toHex(slowAES.decrypt(c,2,a,b)); }";
						InputStreamReader reader2 = new InputStreamReader(FiveSeconds.class.getResourceAsStream("antibot.js"));
						BufferedReader bf =  new BufferedReader(reader2);
						StringBuffer buffer = new StringBuffer();
						String aLine;
						while ( ( aLine = bf.readLine( ) ) != null ) {
							buffer.append(aLine);
						}
						buffer.append(javascript2);
						buffer.append(javascript4);
						initScriptEngine(buffer.toString());
						Object object = invocableEngine.invokeFunction("slowAES_decrypt");
					    CookieStore cookieStore = httpclient.getCookieStore();
					    BasicClientCookie stdCookie = new BasicClientCookie("5giay", object.toString());
					    stdCookie.setDomain(".5giay.vn");
					    stdCookie.setPath("/");
					    cookieStore.addCookie(stdCookie);
					    HttpGet htttGet = new HttpGet("http://www.5giay.vn/login.php?do=login&attempt=1");
						setHeader(htttGet);
						responseBody = httpclient.execute(htttGet, responseHandler);
						System.out.println(responseBody.indexOf(username));
						System.out.println(javascript);
						System.out.println(javascript2);
						System.out.println(javascript3);
						System.out.println(object);
						System.out.println(responseBody);
					}
				}
			}
		}
		return result;
	}

	int maxUpForOneTopic = 3;
	String[] listTopic = new String[] { "22", "126", "145", "140", "14", "24" };

	/* String[] listTopic = new String[] { "89", "44", "55", "41", "28", "43" }; */

	public void selectThread(String topicURL, String topicID, String loginID,
			String message) throws Exception {
		HttpGet httpGetStepOne = new HttpGet(topicURL);
		setHeader(httpGetStepOne);
		String responseBody = httpclient.execute(httpGetStepOne,
				responseHandler);
		// lay ra noi dung xml
		CleanerProperties props = new CleanerProperties();
		// set some properties to non-default values
		props.setTranslateSpecialEntities(true);
		props.setTransResCharsToNCR(true);
		props.setOmitComments(true);
		HtmlCleaner cleaner = new HtmlCleaner(props);
		// do parsing
		TagNode tagNode = cleaner.clean(new StringReader(responseBody));
		PrettyXmlSerializer serializer = new PrettyXmlSerializer(props);
		// serialize to xml file
		String xml = serializer.getAsString(tagNode, "utf-8");
		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(xml));
		// <input type="hidden" value="0" id="vB_Editor_001_mode"
		// name="wysiwyg">
		String xpath = "//tbody[@id='threadbits_forum_"
				+ topicID
				+ "']/tr[child::td/text()='Ä�á»� tÃ i bÃ¬nh thÆ°á»�ng']/./following-sibling::tr/td//a[ string-length(@id) >13 and substring(@id,1,13) = 'thread_title_' and string-length(@href) >17 and substring(@href,1,17)='showthread.php?t=']/@href";

		xpath = "//ol[@id='threads']/li//h3[@class='threadtitle']/a[ string-length(@id) >13 and substring(@id,1,13) = 'thread_title_']/@href";
		List<?> list = document.selectNodes(xpath);
		String url;
		String t;

		for (int i = 0; i < maxUpForOneTopic && i < list.size(); i++) {
			Node element = (Node) list.get(i);
			url = /* "http://www.5giay.vn/" + */element.getText();
			// url = "http://www.5giay.vn/" + element.getText();

			int lastIndex = element.getText().lastIndexOf("/");
			// int lastIndex = element.getText().lastIndexOf("=");
			t = element.getText().substring(lastIndex + 1,
					element.getText().indexOf("-", lastIndex));

			// t = element.getText().substring(lastIndex+1);
			System.out.println("URL: " + url);
			System.out.println("t: " + t);

			setHeader(httpGetStepOne);
			mustWait();
			responseBody = httpclient.execute(httpGetStepOne, responseHandler);
			/**
			 * message Up phÃ¡Â»Â¥ bÃ¡ÂºÂ¡n, rÃ¡ÂºÂ£ng qua up phÃ¡Â»Â¥ mÃƒÂ¬nh vÃ¡Â»Â›i nhÃƒÂ©<br/>
			 * wysiwyg 1<br/>
			 * styleid 0<br/>
			 * signature 1<br/>
			 * fromquickreply 1<br/>
			 * s <br/>
			 * securitytoken 1326680880-d9991c231860f33f173fad8f30595563e8d97b06<br/>
			 * do postreply<br/>
			 * t 4403864<br/>
			 * p who cares<br/>
			 * parseurl 1<br/>
			 * loggedinuser 100080155<br/>
			 * sbutton GÃ¡Â»ÂŸi TrÃ¡ÂºÂ£ LÃ¡Â»Â�i<br/>
			 */
			tagNode = cleaner.clean(new StringReader(responseBody));
			xml = serializer.getAsString(tagNode, "utf-8");
			document = reader.read(new StringReader(xml));
			List<?> list2 = document
					.selectNodes("//input[ @name='securitytoken' ]/@value");
			String securitytoken = null;
			for (Object object1 : list2) {
				Node element1 = (Node) object1;
				securitytoken = element1.getText();
			}
			if (securitytoken != null) {
				// newreply.php?do=postreply&t=4439655
				HttpPost httpPost = new HttpPost(getUrl(POST_URL,
						new Object[] { t }));
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				nvps.add(new BasicNameValuePair("message", message));
				nvps.add(new BasicNameValuePair("wysiwyg", "1"));
				nvps.add(new BasicNameValuePair("styleid", "0"));
				nvps.add(new BasicNameValuePair("signature", "1"));
				nvps.add(new BasicNameValuePair("fromquickreply", "1"));
				nvps.add(new BasicNameValuePair("s", ""));
				nvps.add(new BasicNameValuePair("securitytoken", securitytoken));
				nvps.add(new BasicNameValuePair("do", "postreply"));
				nvps.add(new BasicNameValuePair("t", t));
				nvps.add(new BasicNameValuePair("p", "who cares"));
				nvps.add(new BasicNameValuePair("parseurl", "1"));
				nvps.add(new BasicNameValuePair("loggedinuser", loginID));
				nvps.add(new BasicNameValuePair("sbutton", "Gá»ŸÂŸi Tráº£ Lá»�Â�i"));
				httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
				setHeader(httpPost);
				httpPost.setHeader("Referer",
						"http://www.5giay.vn/showthread.php?t=" + t);
				responseBody = httpclient.execute(httpPost, responseHandler);
				if (responseHandler.isMustFollow()) {
					httpGetStepOne = new HttpGet(responseBody);
					setHeader(httpGetStepOne);
					responseBody = httpclient.execute(httpGetStepOne,
							responseHandler);
				}
				mustWait();
			}
		}

	}

	long maxMustWait = 10;
	long sizeMustWait = 5;

	public synchronized void mustWait() throws InterruptedException {

		long max = maxMustWait;
		long size = sizeMustWait;
		long real = Math.round(max + size * Math.random());
		wait(real * 1000);
	}

	public void up() throws Exception {
		for (int i = 0; i < listTopic.length; i++) {
			selectThread("http://www.5giay.vn/forumdisplay.php?f="
					+ listTopic[i], listTopic[i], "100080155",
					"Up phá»¥ báº¡n, ráº£nh qua up phá»¥ mÃ¬nh link bÃªn dÆ°á»›i nhÃ©!<br/>");
			mustWait();
		}
	}

	public static void main(String[] args) throws Exception {

		FiveSeconds article = new FiveSeconds();
		article.login("myname74119", null, true, new Object[] {
				"25f9e794323b453885f5181f1b624d0b",
				"25f9e794323b453885f5181f1b624d0b" });
		article.up();

		// 57 -WINDOWS 7 :: PHáº¦N Má»€M -GAME
		// XHTTGetArticle getArticle = new XHTTGetArticle();
		// Article article2 =
		// getArticle.get("http://xahoithongtin.com.vn/2011061508031456p0c252/filerfrog-thanh-phan-mo-rong-tuyet-voi-cho-windows-explorer.htm");
		// article2.setContent(article2.getContent()+"Nguá»“n XHTT ");
		// article.post(article2, article.getUrl(PostArticle.POST_FORM_URL, new
		// Object[]{"57"}),
		// article.getUrl(PostArticle.POST_URL, new Object[]{"57"}), null);
		// System.out.println(article.md5hash("123456789")[0]);
		// 25f9e794323b453885f5181f1b624d0b
	}
}
