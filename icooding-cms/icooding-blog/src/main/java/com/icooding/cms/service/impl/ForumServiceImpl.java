package com.icooding.cms.service.impl;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.icooding.cms.model.Forum;
import com.icooding.cms.persistence.ForumDao;
import com.icooding.cms.service.ForumService;

@Service
@Transactional
public class ForumServiceImpl implements ForumService {

	@Autowired
	private ForumDao forumDao;
	
	@Override
	public List<Forum> searchRoot() {
		return forumDao.searchRoot();
	}

	@Override
	public void save(Forum forum) {
		forumDao.save(forum);
	}

	@Override
	public Forum update(Forum forum) {
		return forumDao.update(forum);
	}

	@Override
	public void delete(Forum forum) {
		forumDao.delete(forum);
	}

	@Override
	public Forum find(int id) {
		return forumDao.find(id);
	}

	@Override
	public boolean checkForumName(String forumName) {
		return forumDao.checkForumName(forumName);
	}

	@Override
	public List<Forum> searchChildPoint() {
		return forumDao.searchChildPoint();
	}

	@Override
	public List<Forum> findAll() {
		return forumDao.findAll();
	}
}
