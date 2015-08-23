package com.ansteel.common.attachment.web;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.ansteel.common.attachment.domain.Attachment;
import com.ansteel.common.attachment.domain.AttachmentTree;
import com.ansteel.common.attachment.service.AttachmentService;
import com.ansteel.core.annotation.PathClass;
import com.ansteel.core.annotation.PathDatabaseEntity;
import com.ansteel.core.annotation.QueryJson;
import com.ansteel.core.constant.DHtmlxConstants;
import com.ansteel.core.constant.ViewModelConstant;
import com.ansteel.core.controller.BaseController;
import com.ansteel.core.domain.BaseEntity;
import com.ansteel.core.domain.EntityInfo;
import com.ansteel.core.domain.TreeInfo;
import com.ansteel.core.exception.PageException;
import com.ansteel.core.query.PageUtils;
import com.ansteel.core.query.QuerySettings;
import com.ansteel.core.utils.DownloadUtils;
import com.ansteel.core.utils.QueryMapping;
import com.ansteel.core.utils.ResponseUtils;
import com.ansteel.dhtmlx.jsonclass.UDataSet;
/**
 * 创 建 人：gugu
 * 创建日期：2015-05-28
 * 修 改 人：
 * 修改日 期：
 * 描   述：附件树管理控制器。  
 */
@Controller
@RequestMapping(value = "/att")
public class AttachmentController extends BaseController {

	@Autowired
	AttachmentService attachmentService;

	@Override
	public Collection<EntityInfo> getEntityInfos() {
		// 树设置
		TreeInfo treeInfo = new TreeInfo();
		treeInfo.setTree(AttachmentTree.class);
		Collection<Class> tables = new ArrayList<Class>();
		tables.add(Attachment.class);
		treeInfo.setTables(tables);

		Collection<EntityInfo> eis = new ArrayList<EntityInfo>();
		EntityInfo tree = new EntityInfo();
		tree.setClazz(AttachmentTree.class);
		tree.setTree(treeInfo);
		eis.add(tree);

		EntityInfo table = new EntityInfo();
		table.setClazz(Attachment.class);
		table.setTree(treeInfo);
		eis.add(table);
		return eis;
	}

	@RequestMapping("/a/saveFile/{className}")
	@ResponseBody
	public int saveFileAjax(
			@Valid @PathDatabaseEntity("className") BaseEntity entity,
			BindingResult result,
			@RequestParam(value = "_key", required = false) String key,
			@RequestParam(value = "_value", required = false) String value,
			@RequestParam(value = "file", required = false) MultipartFile file,
			HttpServletRequest request, 
			HttpServletResponse response ){
		if (entity.getClass() == Attachment.class) {
			Attachment attachment = attachmentService.saveAttachment(file,
					value, (Attachment) entity);
			super.saveAjax(attachment, result, key, value, request,response);
			ResponseUtils.setContentType(response);
		} else {
			super.saveAjax(entity, result, key, value, request,response);
		}
		return 0;
	}

	public @ResponseBody
	UDataSet loadPageAjax(
			@PathClass("className") Class clazz,
			@RequestParam(value = "_key", required = false) String key,// 过滤字段名（一般用于主从表）
			@RequestParam(value = "_value", required = false) String value,// 过滤字段值（一般用于主从表）
			@RequestParam(value = "posStart", required = false) String posStart,// 分页当前记录行
			@RequestParam(value = "count", required = false) String count,// 分页记录行
			@RequestParam(value = "_order", required = false) String order,// 排序字段名
			HttpServletRequest request, HttpServletResponse response) {
		UDataSet dataSet = super.loadPageAjax(clazz, key, value, posStart,
				count, order, request, response);

		if (clazz == Attachment.class) {
			return attachmentService.setAttachmentToPath(dataSet,request);
		}
		return dataSet;
	}

	@RequestMapping("/download/{id}")
	public void download(@PathVariable("id") String id,
			@RequestParam(value = "_inline", required = false) String inline,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Attachment attachment = attachmentService.getAttachmentByIdToPath(id);
		DownloadUtils.download(response, attachment.getPath(),attachment.getContentType(),inline);
	}
	
	@RequestMapping("/downloadPath")
	public void downloadPath(@RequestParam(value = "_path", required = false) String path,
			@RequestParam(value = "_inline", required = false) String inline,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		DownloadUtils.download(response,path,null,inline);
	}
	
	public  @ResponseBody UDataSet queryDetailPageAjax(@PathClass("className")Class clazz,
			@RequestParam(value="_key",required=false)String key,//过滤字段名（一般用于主从表）
			@RequestParam(value="_value",required=false)String value,//过滤字段值（一般用于主从表）
			@RequestParam(value="posStart",required=false)String posStart,//分页当前记录行
			@RequestParam(value="count",required=false)String count,//分页记录行
			@RequestParam(value="_order",required=false)String order,//排序字段名
			@QueryJson List<QueryMapping> queryList,
			HttpServletRequest request,
			HttpServletResponse response){
		UDataSet dataSet = super.queryDetailPageAjax(clazz, key, value, posStart, count, order, queryList, request, response);
		if(clazz==Attachment.class) {
			return attachmentService.setAttachmentToPath(dataSet,request);
		}
		return dataSet;
	}

	@RequestMapping("/a/queryPage/{className}")
	public  @ResponseBody UDataSet queryPageAjax(@PathClass("className")Class clazz,
			@RequestParam(value="_key",required=false)String key,//过滤字段名（一般用于主从表）
			@RequestParam(value="_value",required=false)String value,//过滤字段值（一般用于主从表）
			@RequestParam(value="posStart",required=false)String posStart,//分页当前记录行
			@RequestParam(value="count",required=false)String count,//分页记录行
			@RequestParam(value="_order",required=false)String order,//排序字段名
			@QueryJson List<QueryMapping> queryList,
			HttpServletRequest request,
			HttpServletResponse response){
		
		UDataSet dataSet = super.queryPageAjax(clazz, key, value, posStart, count, order, queryList, request, response);
		if(clazz==Attachment.class) {
			return attachmentService.setAttachmentToPath(dataSet,request);
		}
		return dataSet;
	}
}