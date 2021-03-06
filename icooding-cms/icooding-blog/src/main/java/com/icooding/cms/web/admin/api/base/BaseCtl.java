package com.icooding.cms.web.admin.api.base;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.icooding.cms.model.UserSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RequestMapping("/op/base")
public class BaseCtl {

	@RequestMapping("/admin/head")
	public String header(){
		return "admin/base/head";
	}
	
	@RequestMapping("/admin/header")
	public ModelAndView header(HttpSession session){
		ModelAndView mv = new ModelAndView("admin/base/header");
		UserSession userSession = (UserSession) session.getAttribute("userSession");
		if(userSession!=null){
			mv.addObject("nickName", userSession.getUser().getNickName());
		}
		return mv;
	}
	
	@RequestMapping("/ICP")
	public String ICP(){
		return "admin/base/ICP";
	}
	
	@RequestMapping("/footer")
	public String footer(){
		return "admin/base/footer";
	}
	
	@RequestMapping("/foot")
	public String foot(){
		return "admin/base/foot";
	}
	
	@RequestMapping("/loading")
	public ModelAndView loading(int size){
		ModelAndView mv = new ModelAndView("base/loading");
		mv.addObject("size", size);
		return mv;
	}
	
	@RequestMapping("/loginFirst")
	public String loginFirst(HttpServletRequest request){
		if(request.getSession().getAttribute("callback")==null){
			String callback = request.getHeader("REFERER");
			request.getSession().setAttribute("callback", callback);
		}
		return "base/loginFirst";
	}
}
