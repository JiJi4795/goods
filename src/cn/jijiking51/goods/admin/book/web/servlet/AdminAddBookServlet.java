
package cn.jijiking51.goods.admin.book.web.servlet;

import cn.jijiking51.goods.book.domain.Book;
import cn.jijiking51.goods.book.service.BookService;
import cn.jijiking51.goods.category.Service.CategoryService;
import cn.jijiking51.goods.category.domain.Category;
import cn.jijiking51.goods.commons.CommonUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AdminAddBookServlet extends HttpServlet {

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		//设置字符码为utf-8
		request.setCharacterEncoding("utf-8");
		//设置responsecontent为utf-8
		response.setContentType("text/html;charset=utf-8");
		
		/*
		 * 1. commons-fileupload的上传三步
		 */
		// 创建工具 FileItemFactory
		FileItemFactory factory = new DiskFileItemFactory();
		/*
		 * 2. 创建解析器对象 ServletFileUpload，并设置单个最大上传大小
		 */
		ServletFileUpload sfu = new ServletFileUpload(factory);
		//设置单个上传的文件上限为80KB
		sfu.setFileSizeMax(80 * 1024);
		/*
		 * 3. 调用ServletFileUpload#parseRequest解析request得到List<FileItem>
		 */
		List<FileItem> fileItemList = null;
		try {
			fileItemList = sfu.parseRequest(request);
		} catch (FileUploadException e) {
			// 如果出现这个异常，说明单个文件超出了80KB
			error("上传的文件超出了80KB", request, response);
			return;
		}
		
		/*
		 * 4. 把List<FileItem>封装到Book对象中
		 * 4.1 首先把“普通表单字段”放到一个Map中，再把Map转换成Book和Category对象，再建立两者的关系
		 */
		Map<String,Object> map = new HashMap<String,Object>();
		for(FileItem fileItem : fileItemList) {
			//调用fileItem#isFormField查看是否是普通字段，如果是普通表单字段，通过调用getFieldName获得字段名，调用getString获得值，并设置解码‘utf-8’
			if(fileItem.isFormField()) {
				map.put(fileItem.getFieldName(), fileItem.getString("UTF-8"));
			}
		}
		//调用CommonUtils#toBean把map中的属性封装到Book对象
		Book book = CommonUtils.toBean(map, Book.class);
		//调用CommonUtils#toBean把map中的属性封装到Category对象
		Category category = CommonUtils.toBean(map, Category.class);
		//给book设置Category
		book.setCategory(category);
		
		/*
		 * 4.2 把上传的图片保存起来
		 *   > 获取文件名：截取之
		 *   > 给文件添加前缀：使用uuid前缀，为也避免文件同名现象
		 *   > 校验文件的扩展名：只能是jpg
		 *   > 校验图片的尺寸
		 *   > 指定图片的保存路径，这需要使用ServletContext#getRealPath()
		 *   > 保存之
		 *   > 把图片的路径设置给Book对象
		 */
		// 获取文件名
		FileItem fileItem = fileItemList.get(0);//获取大图
		String filename = fileItem.getName();
		// 截取文件名，因为部分浏览器上传的绝对路径
		int index = filename.lastIndexOf("\\");
		if(index != -1) {
			filename = filename.substring(index + 1);
		}
		// 给文件名添加uuid前缀，避免文件同名现象
		filename = CommonUtils.uuid() + "_" + filename;
		// 校验文件名称的扩展名
		if(!filename.toLowerCase().endsWith(".jpg")) {
			error("上传的图片扩展名必须是JPG", request, response);
			return;
		}
		// 校验图片的尺寸
		// 保存上传的图片，把图片new成图片对象：Image、Icon、ImageIcon、BufferedImage、ImageIO
		/*
		 * 保存图片：
		 * 1. 获取真实路径
		 */
		String savepath = this.getServletContext().getRealPath("/book_img");
		/*
		 * 2. 创建目标文件
		 */
		File destFile = new File(savepath, filename);
		/*
		 * 3. 保存文件
		 */
		try {
			fileItem.write(destFile);//它会把临时文件重定向到指定的路径，再删除临时文件
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		// 校验尺寸
		// 1. 使用文件路径创建ImageIcon
		ImageIcon icon = new ImageIcon(destFile.getAbsolutePath());
		// 2. 通过ImageIcon得到Image对象
		Image image = icon.getImage();
		// 3. 获取宽高来进行校验
		if(image.getWidth(null) > 350 || image.getHeight(null) > 350) {
			error("您上传的图片尺寸超出了350*350！", request, response);
			destFile.delete();//删除图片
			return;
		}
		
		// 把图片的路径设置给book对象
		book.setImage_w("book_img/" + filename);
		
		


		// 获取文件名
		fileItem = fileItemList.get(1);//获取小图
		filename = fileItem.getName();
		// 截取文件名，因为部分浏览器上传的绝对路径
		index = filename.lastIndexOf("\\");
		if(index != -1) {
			filename = filename.substring(index + 1);
		}
		// 给文件名添加uuid前缀，避免文件同名现象
		filename = CommonUtils.uuid() + "_" + filename;
		// 校验文件名称的扩展名
		if(!filename.toLowerCase().endsWith(".jpg")) {
			error("上传的图片扩展名必须是JPG", request, response);
			return;
		}
		// 校验图片的尺寸
		// 保存上传的图片，把图片new成图片对象：Image、Icon、ImageIcon、BufferedImage、ImageIO
		/*
		 * 保存图片：
		 * 1. 获取真实路径
		 */
		savepath = this.getServletContext().getRealPath("/book_img");
		/*
		 * 2. 创建目标文件
		 */
		destFile = new File(savepath, filename);
		/*
		 * 3. 保存文件
		 */
		try {
			fileItem.write(destFile);//它会把临时文件重定向到指定的路径，再删除临时文件
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		// 校验尺寸
		// 1. 使用文件路径创建ImageIcon
		icon = new ImageIcon(destFile.getAbsolutePath());
		// 2. 通过ImageIcon得到Image对象
		image = icon.getImage();
		// 3. 获取宽高来进行校验
		if(image.getWidth(null) > 350 || image.getHeight(null) > 350) {
			error("您上传的图片尺寸超出了350*350！", request, response);
			destFile.delete();//删除图片
			return;
		}
		
		// 把图片的路径设置给book对象
		book.setImage_b("book_img/" + filename);
		
		System.out.println(book.getImage_b()+"        "+book.getImage_w());
		
		
		//调用service完成任务
		book.setBid(CommonUtils.uuid());
		BookService bookService = new BookService();
		if(!bookService.add(book)){
			error("请输入正确数值",request,response);
			destFile.delete();
			return ;
		}
	
		// 保存成功信息转发到msg.jsp
		request.setAttribute("msg", "添加图书成功！");
		request.getRequestDispatcher("/adminjsps/msg.jsp").forward(request, response);
	}
	
	/**
	 * 保存错误信息，转发到add.jsp
	 */
	private void error(String msg, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setAttribute("msg", msg);
		//所有一级分类
		request.setAttribute("parents", new CategoryService().findParent());
		request.getRequestDispatcher("/adminjsps/admin/book/add.jsp").
				forward(request, response);
	}
}

