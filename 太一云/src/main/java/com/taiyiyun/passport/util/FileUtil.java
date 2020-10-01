package com.taiyiyun.passport.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.taiyiyun.passport.bean.FileBean;
import com.taiyiyun.passport.consts.Const;

public class FileUtil {
	public final static Map<String, String> FILE_TYPE_MAP = new HashMap<String, String>();    
	
	static {
		 FILE_TYPE_MAP.put("jpg", "FFD8FF"); //JPEG (jpg)    
	        FILE_TYPE_MAP.put("png", "89504E47");  //PNG (png)    
	        FILE_TYPE_MAP.put("gif", "47494638");  //GIF (gif)    
	        FILE_TYPE_MAP.put("tif", "49492A00");  //TIFF (tif)    
	        FILE_TYPE_MAP.put("bmp", "424D"); //Windows Bitmap (bmp)    
	        FILE_TYPE_MAP.put("dwg", "41433130"); //CAD (dwg)    
	        FILE_TYPE_MAP.put("html", "68746D6C3E");  //HTML (html)    
	        FILE_TYPE_MAP.put("rtf", "7B5C727466");  //Rich Text Format (rtf)    
	        FILE_TYPE_MAP.put("xml", "3C3F786D6C");    
	        FILE_TYPE_MAP.put("zip", "504B0304");    
	        FILE_TYPE_MAP.put("rar", "52617221");    
	        FILE_TYPE_MAP.put("psd", "38425053");  //Photoshop (psd)    
	        FILE_TYPE_MAP.put("eml", "44656C69766572792D646174653A");  //Email [thorough only] (eml)    
	        FILE_TYPE_MAP.put("dbx", "CFAD12FEC5FD746F");  //Outlook Express (dbx)    
	        FILE_TYPE_MAP.put("pst", "2142444E");  //Outlook (pst)    
	        FILE_TYPE_MAP.put("xls", "D0CF11E0");  //MS Word    
	        FILE_TYPE_MAP.put("doc", "D0CF11E0");  //MS Excel 注意：word 和 excel的文件头一样    
	        FILE_TYPE_MAP.put("mdb", "5374616E64617264204A");  //MS Access (mdb)    
	        FILE_TYPE_MAP.put("wpd", "FF575043"); //WordPerfect (wpd)     
	        FILE_TYPE_MAP.put("eps", "252150532D41646F6265");    
	        FILE_TYPE_MAP.put("ps", "252150532D41646F6265");    
	        FILE_TYPE_MAP.put("pdf", "255044462D312E");  //Adobe Acrobat (pdf)    
	        FILE_TYPE_MAP.put("qdf", "AC9EBD8F");  //Quicken (qdf)    
	        FILE_TYPE_MAP.put("pwl", "E3828596");  //Windows Password (pwl)    
	        FILE_TYPE_MAP.put("wav", "57415645");  //Wave (wav)    
	        FILE_TYPE_MAP.put("avi", "41564920");    
	        FILE_TYPE_MAP.put("ram", "2E7261FD");  //Real Audio (ram)    
	        FILE_TYPE_MAP.put("rm", "2E524D46");  //Real Media (rm)    
	        FILE_TYPE_MAP.put("mpg", "000001BA");  //    
	        FILE_TYPE_MAP.put("mov", "6D6F6F76");  //Quicktime (mov)    
	        FILE_TYPE_MAP.put("asf", "3026B2758E66CF11"); //Windows Media (asf)    
	        FILE_TYPE_MAP.put("mid", "4D546864");  //MIDI (mid)    
	}
	
	public static final FileBean saveFile(HttpServletRequest request, boolean isAbsolutePath, String savePath, MultipartFile file, String ext) throws Exception {
		if(null == file) {
			throw new Exception("未检测到上传文件");
		}
		
		String originalName = file.getOriginalFilename();
		if(StringUtil.isEmpty(originalName)) {
			throw new Exception("文件名称为空");
		}
		
		if(!checkFileType(originalName,ext)) {
    		throw new Exception("不支持的上传文件类型");
		}
		
		String fileName = Misc.getUUID() + originalName.substring(originalName.lastIndexOf("."));
		String extName = originalName.substring(originalName.lastIndexOf(".") + 1);
		String storeFolder = savePath + "/" + DateUtil.getFileDateTime() + "/";
		storeFolder = storeFolder.replace("//", "/");
		String storePath = isAbsolutePath ? storeFolder : request.getSession().getServletContext().getRealPath(storeFolder);
		
		File folder = new File(storePath + File.separator);
		if (!folder.exists() && !folder.isDirectory()) {
			folder.mkdirs();
		}
		
	
		File targetFile = new File(storePath + File.separator + fileName); 
		try {
			file.transferTo(targetFile);
		} catch (Exception e) {
			throw new Exception("文件保存出现异常");
		}
		
		FileBean fileBean = new FileBean();
		fileBean.setFile(targetFile);
		fileBean.setRelativePath(storeFolder + fileName);
		fileBean.setAbsolutePath(storePath + File.separator + fileName);
		fileBean.setExtName(extName);
		return fileBean;
	}
	
	public static FileBean getThumbnail(HttpServletRequest request, boolean isAbsolutePath, String savePath, MultipartFile file, String ext) throws Exception {
		if(null == file) {
			throw new Exception("未检测到上传文件");
		}
		
		String originalName = file.getOriginalFilename();
		if(StringUtil.isEmpty(originalName)) {
			throw new Exception("文件名称为空");
		}
		
		if(!checkFileType(originalName,ext)) {
    		throw new Exception("不支持的上传文件类型");
		}
		
		String fileName = Misc.getUUID() + originalName.substring(originalName.lastIndexOf("."));
		String storeFolder = savePath + "/" + DateUtil.getFileDateTime() + "/";
		storeFolder = storeFolder.replace("//", "/");
		String storePath = isAbsolutePath ? storeFolder : request.getSession().getServletContext().getRealPath(storeFolder);
		
		File folder = new File(storePath + File.separator);
		if (!folder.exists() && !folder.isDirectory()) {
			folder.mkdirs();
		}
		
		File targetFile = new File(storePath + File.separator + fileName); 
		
		FileBean fileBean = new FileBean();
		fileBean.setFile(targetFile);
		fileBean.setRelativePath(storeFolder + fileName);
		fileBean.setAbsolutePath(storePath + File.separator + fileName);
		return fileBean;
	}
	
	public static FileBean getArticleThumbnail(HttpServletRequest request, boolean isAbsolutePath, String savePath, File file, String ext) throws Exception {
		if(null == file) {
			throw new Exception("未检测到上传文件");
		}
		
		String originalName = file.getName();
		
		String fileName = Misc.getUUID() + originalName.substring(originalName.lastIndexOf("."));
		String storeFolder = savePath + "/" + DateUtil.getFileDateTime() + "/";
		storeFolder = storeFolder.replace("//", "/");
		String storePath = isAbsolutePath ? storeFolder : request.getSession().getServletContext().getRealPath(storeFolder);
		
		File folder = new File(storePath + File.separator);
		if (!folder.exists() && !folder.isDirectory()) {
			folder.mkdirs();
		}
		
		File targetFile = new File(storePath + File.separator + fileName); 
		
		ImageUtil.resizeImage(file, targetFile, Const.ARTICLE_THUMBNAIL_WIDTH, Const.ARTICLE_THUMBNAIL_HEIGHT);
		
		FileBean fileBean = new FileBean();
		fileBean.setFile(targetFile);
		fileBean.setRelativePath(storeFolder + fileName);
		fileBean.setAbsolutePath(storePath + File.separator + fileName);
		return fileBean;
	}
	
	private static boolean checkFileType(String originalName,String ext) {
		boolean isMatch = false;
		if(StringUtil.isEmpty(originalName) || StringUtil.isEmpty(ext)) {
			return isMatch;
		}
		
		String[] exts = ext.split("\\|");
		if (originalName.indexOf(".") < 0) {
			return isMatch;
		}
		
		String fileExt = originalName.substring(originalName.lastIndexOf("."));
		for (int i = 0; i < exts.length; ++i) {
			if (fileExt.equalsIgnoreCase(exts[i].trim())) {
				isMatch = true;
				break;
			}
		}
		
		return isMatch;
	}

	public static final String getFileExt(String fileName) {
		return fileName.substring(fileName.lastIndexOf(".") + 1);
	}

	public static final boolean checkFileType2(String fileName, String... types) {
		boolean flag = false;
		String type = fileName.substring(fileName.lastIndexOf(".") + 1);
		for(String str : types) {
			if(StringUtils.equalsIgnoreCase(str, type)) {
				flag = true;
				break;
			}
		}
		return flag;
	}

	public static final boolean checkPictures(String ... picNames) {
		boolean flag = true;
		for(String picName : picNames) {
			if (!checkFileType2(picName, "jpg", "jpeg", "png")) {
				return false;
			}
		}
		return flag;
	}

	public static final boolean multipartFileIsNull(MultipartFile file) {
		if(null == file) {
			return true;
		}
		
		try {
			if(null == file.getBytes() || file.getBytes().length == 0) {
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return true;
		}
		return false;
	}
	
	public final static String getFileTypeByByte(byte[] b) {
		String filetypeHex = String.valueOf(getFileHexString(b));
		
		Iterator<Entry<String, String>> entryiterator = FILE_TYPE_MAP.entrySet().iterator();
		while (entryiterator.hasNext()) {
			Entry<String, String> entry = entryiterator.next();
			String fileTypeHexValue = entry.getValue();
			
			if (filetypeHex.toUpperCase().startsWith(fileTypeHexValue)) {
				return entry.getKey();
			}
		}
		return null;
	}

	public final static String getFileHexString(byte[] b) {
		StringBuilder stringBuilder = new StringBuilder();
		if (b == null || b.length <= 0) {
			return null;
		}
		
		for (int i = 0; i < b.length; i++) {
			int v = b[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		
		return stringBuilder.toString();
	}

	public static void copyTo(byte[] srcFile, String destFilePath) throws IOException {
		File outFile = new File(destFilePath);
		
		OutputStream os = null;
		try {
			if (!outFile.getParentFile().exists()) {
				outFile.getParentFile().mkdirs();
			}
			
			if (!outFile.exists()) {
				outFile.createNewFile();
			}
			
			os = new FileOutputStream(outFile);
			os.write(srcFile);
			os.flush();
		} finally {
			if(os != null) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static final boolean exists(File file) {
		return file.exists();
	}

	public static final boolean exists(String filePath) {
		return exists(new File(filePath));
	}

	public static String getFileNameWithoutExtension(String fileName) {
		if (StringUtil.isEmpty(fileName)) {
			return null;
		}
		
		if (fileName.indexOf(".") > -1) {
			return fileName.substring(0, fileName.lastIndexOf("."));
		}
		
		return fileName;
	}
	
}
