package com.icooding.cms.service;

import com.icooding.cms.model.Media;

public interface MediaService {

	public Media find(int id);
	
	public void save(Media media);
	
	public Media update(Media media);
	
	public void delete(Media media);
	
	public Media findByUrl(String url);
}
