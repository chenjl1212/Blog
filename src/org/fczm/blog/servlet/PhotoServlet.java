package org.fczm.blog.servlet;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.fczm.blog.dao.BlogDao;
import org.fczm.blog.dao.IllustrationDao;
import org.fczm.blog.domain.Blog;
import org.fczm.blog.domain.Illustration;
import org.fczm.blog.service.util.ManagerTemplate;
import org.fczm.common.util.FileTool;
import org.fczm.common.util.ImageTool;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import net.sf.json.JSONObject;

@WebServlet("/PhotoServlet")
public class PhotoServlet extends HttpServlet {
	private static final long serialVersionUID = -323346500850076971L;

	//封面宽度
	public static final int MAX_IMAGE_WIDTH=1440;

	//压缩格式
	public static final String PHOTO_FORMAT=".jpg";
	//上传路径
	public static final String UPLOAD_FOLDER="upload";
	//上传文件大小限制
	private static final int FILE_MAX_SIZE=512*1024*1024;
	
	private String rootPath;
	private String task;
       
    public PhotoServlet() {
        super();
       
    }
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		task=request.getParameter("task");
		
		switch (task) {
		case "clearUnusefulPhotos":
			clearUnusefulPhotos(request,response);
			break;
		default:
			break;
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		task=request.getParameter("task");
		rootPath=getServletConfig().getServletContext().getRealPath("/")+File.separator;
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/json");
		switch (task) {
		case "uploadBlogCover":
			uploadBlogCover(request,response);
			break;
		case "uploadIllustration":
			uploadIllustration(request, response);
			break;
		default:
			break;
		}
	}

	private void uploadBlogCover(HttpServletRequest request, HttpServletResponse response) throws IOException {
		JSONObject data=new JSONObject();
		String bid=request.getParameter("bid");
		WebApplicationContext context= WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		ManagerTemplate template=(ManagerTemplate)context.getBean("managerTemplate");
		BlogDao blogDao=template.getBlogDao();
		Blog blog=blogDao.get(bid);
		if(blog==null) {
			data.put("success", false);
			response.getWriter().print(data.toString());
			return;
		}
		String filepath=createUploadPhotoDirectory(bid);
		String fileName=upload(request, filepath);
		//删除旧封面
		if(blog.getCover()!=null) {
			new File(filepath+File.separator+blog.getCover()).delete();
		}
		blog.setCover(UUID.randomUUID().toString()+PHOTO_FORMAT);
		blogDao.update(blog);
		//文件改名
		FileTool.modifyFileName(filepath, fileName, blog.getCover());
		//当上传图片宽度大于规定的最大宽度，就等比例压缩图片
		String pathname=filepath+File.separator+blog.getCover();
		int width=ImageTool.getImageWidth(pathname);
		int height=ImageTool.getImageHeight(pathname);
		if(width>MAX_IMAGE_WIDTH) {
			ImageTool.createThumbnail(pathname, MAX_IMAGE_WIDTH, MAX_IMAGE_WIDTH*height/width, 0);
		}
		data.put("cover", blog.getCover());
		data.put("success", true);
		response.getWriter().print(data.toString());
	}

	private void uploadIllustration(HttpServletRequest request, HttpServletResponse response) throws IOException {
		JSONObject data=new JSONObject();
		String bid=request.getParameter("bid");
		WebApplicationContext context= WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		ManagerTemplate template=(ManagerTemplate)context.getBean("managerTemplate");
		BlogDao blogDao=template.getBlogDao();
		IllustrationDao illustrationDao=template.getIllustrationDao();
		Blog blog=blogDao.get(bid);
		if(blog==null) {
			data.put("success", false);
			response.getWriter().print(data.toString());
			return;
		}
		String filepath=createUploadPhotoDirectory(bid);
		String fileName=upload(request, filepath);
		Illustration illustration=new Illustration();
		illustration.setFilename(UUID.randomUUID().toString()+PHOTO_FORMAT);
		illustration.setUpload(new Date());
		illustration.setBlog(blog);
		illustrationDao.save(illustration);
		//文件改名
		FileTool.modifyFileName(filepath, fileName, illustration.getFilename());
		//当上传图片宽度大于规定的最大宽度，就等比例压缩图片
		String pathname=filepath+File.separator+illustration.getFilename();
		int width=ImageTool.getImageWidth(pathname);
		int height=ImageTool.getImageHeight(pathname);
		if(width>MAX_IMAGE_WIDTH) {
			ImageTool.createThumbnail(pathname, MAX_IMAGE_WIDTH, MAX_IMAGE_WIDTH*height/width, 0);
		}
		data.put("filename", illustration.getFilename());
		data.put("success", true);
		response.getWriter().print(data.toString());
	}
	
	/**
	 * 如果需要的照片上传路径不存在，则创建
	 * @param id
	 * @return 上传文件路径
	 */
	private String createUploadPhotoDirectory(String id) {
		String filepath=rootPath+UPLOAD_FOLDER+File.separator+id;
		//如果不存文件夹，新建文件夹
		FileTool.createDirectoryIfNotExsit(filepath);
		return filepath;
	}

	/**
	 * 指定路径上传文件
	 * @param request HttpServletRequest
	 * @param filepath 文件路径
	 * @return 文件名
	 */
	@SuppressWarnings("unchecked")
	private String upload(HttpServletRequest request,String filepath) {
		String fileName=null;
		DiskFileItemFactory factory = new DiskFileItemFactory();//为文件对象产生工厂对象。
		factory.setSizeThreshold(1024*4); //设置缓冲区的大小，此处为4kb
		factory.setRepository(new File(filepath)); //设置上传文件的目的地
		ServletFileUpload upload = new ServletFileUpload(factory);//产生servlet上传对象
		upload.setSizeMax(FILE_MAX_SIZE);  //设置上传文件的大小
		try {
			List<FileItem> list=upload.parseRequest(request); //取得所有的上传文件信息
			Iterator<FileItem> it=list.iterator();
			while(it.hasNext()) {
			    FileItem item=it.next();
			    if(item.isFormField()==false) { 
				    fileName=item.getName();   //文件名
				    //取文件名  
				    fileName=fileName.substring(fileName.lastIndexOf(File.separator)+1, fileName.length());               
				    if(!fileName.equals("")&&!(fileName==null)) {
				    	//如果fileName为null，即没有上传文件  
				    	File uploadedFile=new File(filepath,fileName);  
				        try {
				        	item.write(uploadedFile);
				        } catch (Exception e) {
				        	e.printStackTrace();
				        }  
				    }            
			    }
			}
		} catch (FileUploadException e) {
			e.printStackTrace();
		}
		return fileName;
	}

	private void clearUnusefulPhotos(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
	}
	
}