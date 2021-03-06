package com.icooding.cms.web.admin.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.zxing.common.StringUtils;
import com.icooding.cms.dto.GlobalSetting;
import com.icooding.cms.dto.Index;
import com.icooding.cms.model.*;
import com.icooding.cms.service.NaviService;
import com.icooding.cms.service.ParamService;
import com.icooding.cms.service.ThirdPartyAccessService;
import com.icooding.cms.web.base.Constants;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RequestMapping("/admin/siteSet")
public class SiteSetCtl {

    private static final Logger LOG = Logger.getLogger(SiteSetCtl.class);
    
	@Autowired
	private ThirdPartyAccessService thirdPartyAccessService;
	
	@Autowired
	private ParamService paramService;
	
	@Autowired
	private NaviService naviService;
	
	/**
	 * 站点基本信息
	 * @return
	 */
	@RequestMapping("/siteInfo")
	public ModelAndView siteInfo() {
		ModelAndView mv = new ModelAndView("admin/siteSet/siteInfo");
		return mv;
	}

	@RequestMapping(value = "/updateSiteInfo", method = RequestMethod.POST)
	@ResponseBody
	public Object updateSiteInfo(HttpSession session, String siteName,
			String appName, String appUrl, String adminEmail, String icp, String appEnName,
			String statistics, String statisticsHead, boolean redisOpen,boolean grant) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			GlobalSetting siteSet = GlobalSetting.getInstance();
			siteSet.setSiteName(siteName);
			siteSet.setAppName(appName);
			siteSet.setAppUrl(appUrl);
			siteSet.setAdminEmail(adminEmail);
			siteSet.setIcp(icp);
			siteSet.setAppEnName(appEnName);
			siteSet.setStatistics(statistics);
			siteSet.setStatisticsHead(statisticsHead);
			siteSet.setRedisOpen(redisOpen);
			siteSet.setGrant(grant);
			updateParam(Constants.SITE_NAME, siteName, Param.TYPE_TEXT);
			updateParam(Constants.APP_NAME, appName, Param.TYPE_TEXT);
			updateParam(Constants.APP_URL, appUrl, Param.TYPE_TEXT);
			updateParam(Constants.ADMIN_EMAIL, adminEmail, Param.TYPE_TEXT);
			updateParam(Constants.ICP, icp, Param.TYPE_TEXT);
			updateParam(Constants.APP_EN_NAME, appEnName, Param.TYPE_TEXT);
			updateParam(Constants.STATISTICS, statistics, Param.TYPE_TEXT);
			updateParam(Constants.STATISTICSHEAD, statisticsHead, Param.TYPE_TEXT);
			updateParam(Constants.REDIS_OPEN, redisOpen?1:0, Param.TYPE_INT);
			updateParam(Constants.GRANT, grant?1:0, Param.TYPE_INT);
			map.put("success", true);
			map.put("msg", Constants.UPDATE_SET_SUCCESS);
		} catch (Exception e) {
			LOG.error(Constants.UPDATE_SET_FAIL, e);
			map.put("success", false);
			map.put("msg", Constants.UPDATE_SET_FAIL);
		}
		return map;
	}

	public void updateParam(String key, Object value, int type){
		Param pobj = paramService.findByKey(key);
		if(pobj==null){
			pobj = new Param();
			pobj.setKey(key);
			pobj.setType(type);
		}
		if(type==Param.TYPE_TEXT){
			pobj.setTextValue((String) value);
		}else{
			pobj.setIntValue((int) value);
		}
		paramService.update(pobj);
	}
	
	/**
	 * 注册控制页面
	 * @return
	 */
	@RequestMapping("/reg")
	public ModelAndView reg() {
		ModelAndView mv = new ModelAndView("admin/siteSet/reg");
		return mv;
	}

	@RequestMapping("/updateReg")
	@ResponseBody
	public Object updateReg(HttpSession session, boolean on, boolean emailOn,
			int minLengthOfPassword) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			GlobalSetting siteSet = GlobalSetting.getInstance();
			siteSet.setRegAllow(on);
			siteSet.setNeedEmailVerify(emailOn);
			siteSet.setMinLengthOfPassword(minLengthOfPassword);
			updateParam(Constants.REG_ALLOW, on?1:0, Param.TYPE_INT);
			updateParam(Constants.NEED_EMAIL_VERIFY, emailOn?1:0, Param.TYPE_INT);
			map.put("success", true);
			map.put("msg", Constants.UPDATE_SET_SUCCESS);
		} catch (Exception e) {
		    LOG.error(Constants.UPDATE_SET_FAIL, e);
            map.put("success", false);
            map.put("msg", Constants.UPDATE_SET_FAIL);
		}
		return map;
	}

	/**
	 * 邮件服务器设置
	 * @param request
	 * @return
	 */
	@RequestMapping("/mail")
	public ModelAndView mail(HttpServletRequest request) {
		ModelAndView mv = new ModelAndView("admin/siteSet/mail");
        // Properties prop = new Properties();
        Param smtp_server = paramService.findByKey(Constants.SMTP_SERVER);
        Param smtp_from = paramService.findByKey(Constants.SMTP_FROM);
        Param smtp_username = paramService.findByKey(Constants.SMTP_USERNAME);
        Param smtp_password = paramService.findByKey(Constants.SMTP_PASSWORD);
        Param smtp_timeout = paramService.findByKey(Constants.SMTP_TIMEOUT);
        String from = smtp_from == null ? "" : smtp_from.getTextValue();
        String host = smtp_server == null ? "" : smtp_server.getTextValue();
        String password = smtp_password == null ? "" : smtp_password.getTextValue();
        // String auth = prop.getProperty( "mail.smtp.auth" ).trim();
        String timeout = smtp_timeout == null ? "" : smtp_timeout.getTextValue();
        String username = smtp_username == null ? "" : smtp_username.getTextValue();
        mv.addObject("from", from);
        mv.addObject("host", host);
        mv.addObject("password", password);
        mv.addObject("timeout", timeout);
        mv.addObject("username", username);
		return mv;
	}

	@RequestMapping("/updateMail")
	@ResponseBody
	public Object updateMail(HttpSession session, String host, String from,
			String username, String password, String timeout,
			HttpServletRequest request) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			Param smtp_server = paramService.findByKey(Constants.SMTP_SERVER);
			if(smtp_server==null){
				smtp_server = new Param();
				smtp_server.setType(Param.TYPE_TEXT);
				smtp_server.setKey(Constants.SMTP_SERVER);
			}
			smtp_server.setTextValue(host);
			paramService.update(smtp_server);
			Param smtp_from = paramService.findByKey(Constants.SMTP_FROM);
			if(smtp_from==null){
				smtp_from = new Param();
				smtp_from.setType(Param.TYPE_TEXT);
				smtp_from.setKey(Constants.SMTP_FROM);
			}
			smtp_from.setTextValue(from);
			paramService.update(smtp_from);
			Param smtp_username = paramService.findByKey(Constants.SMTP_USERNAME);
			if(smtp_username==null){
				smtp_username = new Param();
				smtp_username.setType(Param.TYPE_TEXT);
				smtp_username.setKey(Constants.SMTP_USERNAME);
			}
			smtp_username.setTextValue(username);
			paramService.update(smtp_username);
			Param smtp_password = paramService.findByKey(Constants.SMTP_PASSWORD);
			if(smtp_password==null){
				smtp_password = new Param();
				smtp_password.setType(Param.TYPE_TEXT);
				smtp_password.setKey(Constants.SMTP_PASSWORD);
			}
			smtp_password.setTextValue(password);
			paramService.update(smtp_password);
			Param smtp_timeout = paramService.findByKey(Constants.SMTP_TIMEOUT);
			if(smtp_timeout==null){
				smtp_timeout = new Param();
				smtp_timeout.setType(Param.TYPE_TEXT);
				smtp_timeout.setKey(Constants.SMTP_TIMEOUT);
			}
			smtp_timeout.setTextValue(timeout);
			paramService.update(smtp_timeout);
			JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
			javaMailSender.setHost(smtp_server.getTextValue());
			javaMailSender.setUsername(smtp_username.getTextValue());
			javaMailSender.setPassword(smtp_password.getTextValue());
			javaMailSender.setDefaultEncoding("UTF-8");
			Properties p = new Properties();
			p.setProperty("mail.smtp.auth", "true");
			p.setProperty("mail.smtp.timeout", smtp_timeout.getTextValue()==null?"":smtp_timeout.getTextValue());
			
			javaMailSender.setJavaMailProperties(p);
			GlobalSetting siteSet = GlobalSetting.getInstance();
			siteSet.setJavaMailSender(javaMailSender);
			siteSet.setSmtpFrom(smtp_from.getTextValue());
			map.put("success", true);
			map.put("msg", Constants.UPDATE_SET_SUCCESS);
//			map.put("success", false);
//			map.put("msg", "木有改动");
		} catch (Exception e) {
		    LOG.error(Constants.UPDATE_SET_FAIL, e);
            map.put("success", false);
            map.put("msg", Constants.UPDATE_SET_FAIL);
		}
		return map;
	}

	/**
	 * 发送测试邮件
	 * @param session
	 * @return
	 */
	@RequestMapping("/sendTestEmail")
	@ResponseBody
	public Object sendTestEmail(HttpSession session) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			// 建立邮件消息
			GlobalSetting setting = (GlobalSetting) session.getAttribute("setting");
			MimeMessage mailMessage = setting.getJavaMailSender().createMimeMessage();
			MimeMessageHelper messageHelper = new MimeMessageHelper(mailMessage);
			GlobalSetting siteSet = (GlobalSetting) session.getAttribute("setting");
			// 设置收件人，寄件人 用数组发送多个邮件
			String[] array = new String[]{siteSet.getAdminEmail()};
			// mailMessage.setTo(array);
			messageHelper.setTo(array);
			messageHelper.setFrom(setting.getSmtpFrom());
			messageHelper.setSubject(siteSet.getAppName()+"邮箱发送测试（请勿回复此邮件）");
			messageHelper
					.setText(
							"<!doctype html>"
									+ "<html>"
									+ "<head>"
									+ "<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>"
									+ "<title>"+siteSet.getAppName()+"STMP测试邮件</title>"
									+ "</head>"
									+ "<body>"
									+ "<div style='margin:0 auto;width:650px;'>"
									+ "<p>恭喜，SMTP发信成功！</span>"
									+ "</p>" + "</div>" + "</body>" + "</html>",
							true);
			setting.getJavaMailSender().send(mailMessage);
			map.put("success", true);
			map.put("msg", "发送成功");
		}catch(AuthenticationFailedException e){
		    LOG.error("SMTP授权失败", e);
			map.put("msg", "SMTP授权失败");
			map.put("success", false);
		}
		catch (MessagingException e) {
		    LOG.error("邮件发送失败失败", e);
			map.put("success", false);
			map.put("msg", "邮件发送失败");
		}
		return map;
	} 

	/**
	 * QQ登录设置
	 * @return
	 */
	@RequestMapping("/qq")
	public ModelAndView qq() {
		ModelAndView mv = new ModelAndView("admin/siteSet/qq");
		ThirdPartyAccess qq = thirdPartyAccessService.findByType(ThirdPartyAccess.TYPE_QQ);
		mv.addObject("qq", qq);
		return mv;
	}

	@RequestMapping("/updateqq")
	@ResponseBody
	public Object updateqq(HttpSession session, boolean on, String accessKey,
			String accessSecret,HttpServletRequest request) {
		Map<String, Object> map = new HashMap<String, Object>();
		ServletContext context = request.getServletContext();
		try {
			GlobalSetting siteSet = GlobalSetting.getInstance();
			siteSet.setQq(on);
			updateParam(Constants.QQ, on?1:0, Param.TYPE_INT);
			if(on){
				ThirdPartyAccess qq = thirdPartyAccessService.findByType(ThirdPartyAccess.TYPE_QQ);
				if(qq==null){
					qq = new ThirdPartyAccess();
					qq.setType(ThirdPartyAccess.TYPE_QQ);
				}
				qq.setAccessKey(accessKey);
				qq.setAccessSecret(accessSecret);
				thirdPartyAccessService.update(qq);
			}
			Properties prop = new Properties();
			String relpath = context.getRealPath("/") + File.separator;
			File file = new File(relpath + "WEB-INF/classes/qqconnectconfig.properties");
			InputStream in = new FileInputStream(file);
			prop.load(in);
			prop.setProperty("app_ID", accessKey);
			prop.setProperty("app_KEY", accessSecret);
			prop.setProperty("redirect_URI", "http://"+siteSet.getAppUrl()+"/op/login/QQLogin");
			OutputStream os = new FileOutputStream(file);
			prop.store(os, "更新QQ互联设置");
			os.close();
			map.put("success", true);
			map.put("msg", Constants.UPDATE_SET_SUCCESS);
			in.close();
		} catch (Exception e) {
		    LOG.error(Constants.UPDATE_SET_FAIL, e);
            map.put("success", false);
            map.put("msg", Constants.UPDATE_SET_FAIL);
		}
		return map;
	}
	
	/**
	 * 新浪微博登录设置
	 * @return
	 */
	@RequestMapping("/xinlang")
	public ModelAndView xinlang() {
		ModelAndView mv = new ModelAndView("admin/siteSet/xinlang");
		ThirdPartyAccess xinlang = thirdPartyAccessService.findByType(ThirdPartyAccess.TYPE_XINLANG);
		mv.addObject("xinlang", xinlang);
		return mv;
	}

	@RequestMapping("/updatexinlang")
	@ResponseBody
	public Object updatexinlang(HttpSession session, boolean on, String accessKey,
			String accessSecret) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			GlobalSetting siteSet = GlobalSetting.getInstance();
			siteSet.setWeibo(on);
			updateParam(Constants.WEIBO, on?1:0, Param.TYPE_INT);
			if(on){
				ThirdPartyAccess xinlang = thirdPartyAccessService.findByType(ThirdPartyAccess.TYPE_XINLANG);
				if(xinlang==null){
					xinlang = new ThirdPartyAccess();
					xinlang.setType(ThirdPartyAccess.TYPE_XINLANG);
				}
				xinlang.setAccessKey(accessKey);
				xinlang.setAccessSecret(accessSecret);
				thirdPartyAccessService.update(xinlang);
			}
			map.put("success", true);
			map.put("msg", Constants.UPDATE_SET_SUCCESS);
		} catch (Exception e) {
		    LOG.error(Constants.UPDATE_SET_FAIL, e);
            map.put("success", false);
            map.put("msg", Constants.UPDATE_SET_FAIL);
		}
		return map;
	}
	
	/**
	 * 首页SEO设置
	 * @return
	 */
	@RequestMapping("/seo")
	public ModelAndView seo(){
		ModelAndView mv = new ModelAndView("admin/siteSet/seo");
		Param title = paramService.findByKey(Constants.SEO_INDEX_TITLE);
		Param keywords = paramService.findByKey(Constants.SEO_INDEX_KEYWORDS);
		Param description = paramService.findByKey(Constants.SEO_INDEX_DESCRIPTION);
		mv.addObject("title", title!=null?title.getTextValue():"");
		mv.addObject("keywords", keywords!=null?keywords.getTextValue():"");
		mv.addObject("description", description!=null?description.getTextValue():"");
		return mv;
	}
	
	@RequestMapping("/updateSeo")
	@ResponseBody
	public Object updateSeo(String title, String keywords, String description){
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			Param t = paramService.findByKey(Constants.SEO_INDEX_TITLE);
			if(t==null){
				t = new Param();
				t.setType(Param.TYPE_TEXT);
				t.setKey(Constants.SEO_INDEX_TITLE);
			}
			t.setTextValue(title);
			paramService.update(t);
			Param k = paramService.findByKey(Constants.SEO_INDEX_KEYWORDS);
			if(k==null){
				k = new Param();
				k.setType(Param.TYPE_TEXT);
				k.setKey(Constants.SEO_INDEX_KEYWORDS);
			}
			k.setTextValue(keywords);
			paramService.update(k);
			Param d = paramService.findByKey(Constants.SEO_INDEX_DESCRIPTION);
			if(d==null){
				d = new Param();
				d.setType(Param.TYPE_TEXT);
				d.setKey(Constants.SEO_INDEX_DESCRIPTION);
			}
			d.setTextValue(description);
			paramService.update(d);
			Index index = Index.getInstance();
			index.setTitle(title!=null?t.getTextValue():"");
			index.setKeywords(keywords!=null?k.getTextValue():"");
			index.setDescription(description!=null?d.getTextValue():"");
			map.put("success", true);
			map.put("msg", Constants.UPDATE_SET_SUCCESS);
		} catch (Exception e) {
		    LOG.error(Constants.UPDATE_SET_FAIL, e);
            map.put("success", false);
            map.put("msg", Constants.UPDATE_SET_FAIL);
		}
		return map;
	}
	
	@RequestMapping("/naviSet")
	public ModelAndView naviSet(){
		ModelAndView mv = new ModelAndView("admin/siteSet/naviSet");
		return mv;
	}
	
	@RequestMapping("/getNaviRoot")
	@ResponseBody
	public Object getNaviRoot(){
		List<Navi> navis = naviService.searchRoot();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (Navi n : navis) {
			list.add(naviToJson(n));
		}
		return list;
	}
	
	public Map<String, Object> naviToJson(Navi navi){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", navi.getId());
		map.put("name", navi.getName());
		map.put("url", navi.getUrl());
		map.put("type", navi.getType());
		map.put("order", navi.getOrder());
		map.put("target", navi.getTarget());
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for(Navi n:navi.getChilds()){
			list.add(naviToJson(n));
		}
		map.put("children", list);
		return map;
	}
	
	@RequestMapping("/updateNavi")
	@ResponseBody
	public Object updateNavi(String result){
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			JSONArray array = new JSONArray(result);
			for(int i=0;i<array.length();i++){
				JSONObject obj = (JSONObject) array.get(i);
				int id = obj.getInt("id");
				int parentId = obj.getInt("parentId");
				int type = obj.getInt("type");
				int order = obj.getInt("order");
				String url = obj.getString("url");
				String name = obj.getString("name");
				String target = obj.getString("target");
				Navi navi = naviService.find(id);
				if(navi==null){
					navi = new Navi();
					navi.setType(type);
					if(parentId!=0){
						Navi parent = naviService.find(parentId);
						navi.setParent(parent);
					}
				}
				navi.setUrl(url);
				navi.setTarget(org.apache.commons.lang.StringUtils.equals(target,"_blank") ?Navi.NaviTarget._blank:Navi.NaviTarget._self);
				navi.setName(name);
				navi.setOrder(order);
				naviService.update(navi);
			}
			autoUpdateNavi();
			map.put("success", true);
		} catch (JSONException e) {
		    LOG.error(Constants.UPDATE_SET_FAIL, e);
            map.put("success", false);
            map.put("msg", Constants.UPDATE_SET_FAIL);
		}
		return map;
	}
	
	@RequestMapping("/delNavi")
	@ResponseBody
	public Object delNavi(int id){
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			naviService.delete(naviService.find(id));
			autoUpdateNavi();
			map.put("success", true);
		} catch (Exception e) {
		    LOG.error("删除导航失败", e);
            map.put("success", false);
		}
		return map;
	}
	
	public void autoUpdateNavi(){
		Index index = Index.getInstance();
		List<Navi> navis = naviService.searchRoot();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (Navi n : navis) {
			list.add(naviToJson(n));
		}
		index.setNavis(list);
	}
	
	@RequestMapping("/changyan")
    public ModelAndView duoshuo(){
        ModelAndView mv = new ModelAndView("admin/siteSet/changyan");
        return mv;
    }
	
	@RequestMapping("/updateChangyan")
	@ResponseBody
	public Object updateChangyan(String changyanAppId, String changyanSecret){
	    Map<String, Object> map = new HashMap<String, Object>();
	    try{
    	    GlobalSetting globalSetting = GlobalSetting.getInstance();
    	    globalSetting.setChangyanAppId(changyanAppId);
    	    globalSetting.setChangyanSecret(changyanSecret);
    	    updateParam(Constants.CHANGYAN_APP_ID, changyanAppId, Param.TYPE_TEXT);
    	    updateParam(Constants.CHANGYAN_SECRET, changyanSecret, Param.TYPE_TEXT);
    	    map.put("success", true);
            map.put("msg", Constants.UPDATE_SET_SUCCESS);
        } catch (Exception e) {
            LOG.error(Constants.UPDATE_SET_FAIL, e);
            map.put("success", false);
            map.put("msg", Constants.UPDATE_SET_FAIL);
        }
        return map;
	}
	
	@RequestMapping("/baiduShare")
    public ModelAndView baiduShare(){
        ModelAndView mv = new ModelAndView("admin/siteSet/baiduShare");
        ThirdPartyAccess weibo = thirdPartyAccessService.findByType(ThirdPartyAccess.TYPE_XINLANG);
        if(weibo!=null){
            mv.addObject("weibo", weibo.getAccessKey());
        }
        return mv;
    }
    
    @RequestMapping("/updateBaiduShare")
    @ResponseBody
    public Object updateBaiduShare(String txAppKey){
        Map<String, Object> map = new HashMap<String, Object>();
        try{
            GlobalSetting globalSetting = GlobalSetting.getInstance();
            globalSetting.setTxAppKey(txAppKey);
            updateParam(Constants.TX_APP_KEY, txAppKey, Param.TYPE_TEXT);
            map.put("success", true);
            map.put("msg", Constants.UPDATE_SET_SUCCESS);
        } catch (Exception e) {
            LOG.error(Constants.UPDATE_SET_FAIL, e);
            map.put("success", false);
            map.put("msg", Constants.UPDATE_SET_FAIL);
        }
        return map;
    }
    
    @RequestMapping("/sms")
    public ModelAndView sms(){
        ModelAndView mv = new ModelAndView("admin/siteSet/sms");
        return mv;
    }
    
    @RequestMapping("/updateSms")
    @ResponseBody
    public Object updateSms(String smsKey){
        Map<String, Object> map = new HashMap<String, Object>();
        try{
            GlobalSetting globalSetting = GlobalSetting.getInstance();
            globalSetting.setSmsKey(smsKey);
            updateParam(Constants.SMS_KEY, smsKey, Param.TYPE_TEXT);
            map.put("success", true);
            map.put("msg", Constants.UPDATE_SET_SUCCESS);
        } catch (Exception e) {
            LOG.error(Constants.UPDATE_SET_FAIL, e);
            map.put("success", false);
            map.put("msg", Constants.UPDATE_SET_FAIL);
        }
        return map;
    }

}
