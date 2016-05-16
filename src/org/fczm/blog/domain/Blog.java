package org.fczm.blog.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Blog implements Serializable {
	
	private static final long serialVersionUID = 4114418330696173690L;

	private String bid; 
	private String title;
	private String content; 
	private Date date;
	private Integer readers;
	private String cover;
	private Type type;
	private List<Comment> comments;
	
	public String getBid() {
		return bid;
	}
	public void setBid(String bid) {
		this.bid = bid;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public Integer getReaders() {
		return readers;
	}
	public void setReaders(Integer readers) {
		this.readers = readers;
	}
	public String getCover() {
		return cover;
	}
	public void setCover(String cover) {
		this.cover = cover;
	}
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	public List<Comment> getComments() {
		return comments;
	}
	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}
	
}
