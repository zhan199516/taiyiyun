package com.taiyiyun.passport.commons;

import com.taiyiyun.passport.consts.Config;
import com.taiyiyun.passport.util.DateUtil;
import com.taiyiyun.passport.util.FileUtil;
import com.taiyiyun.passport.util.StringUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Created by nina on 2017/11/21.
 */
public abstract class PassPortUtil {
    private static final Log log = LogFactory.getLog(PassPortUtil.class);

    public static String saveIdCardBack(String imgBase64Back_NoSign, String oldPath) throws IOException {
        String filePath = "";
        try {
            filePath = saveBase64File(imgBase64Back_NoSign, oldPath);
        } catch (IOException e) {
            log.error("保存身份证背面照片失败：" + e.getMessage());
            throw e;
        }
        return filePath;
    }
    
    public static String saveIdCardFront(String imgBase64Back_NoSign, String oldPath) throws IOException {
        String filePath = "";
        try {
            filePath = saveBase64File(imgBase64Back_NoSign, oldPath);
        } catch (IOException e) {
            log.error("保存身份证正面照片失败：" + e.getMessage());
            throw e;
        }
        return filePath;
    }

    public static String saveGroupHeader(String groupHeader, String oldPath) throws IOException {
        String filePath = "";
        try {
            filePath = saveGroupHeaderBase64File(groupHeader, oldPath);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("保存群头像出现异常：" + e.getMessage());
            throw e;
        }
        return filePath;
    }

    private static final String saveGroupHeaderBase64File(String base64File, String oldPath) throws IOException{
        String filePath = "";
        String groupHeaderFile = "";
        String groupheaderroot = Config.get("groupheaderroot");
        String groupheaderdir = Config.get("groupheaderdir");
        if(oldPath == null) {
            Date now = new Date();
            String datePath = DateUtil.toString(now, "yyyy") + File.separator + DateUtil.toString(now, "MM") + File.separator + DateUtil.toString(now, "dd") + File.separator;
            String dir = groupheaderroot + groupheaderdir + datePath;
            File dirFile = new File(dir);
            if(!dirFile.exists()) {
                dirFile.mkdirs();
            }
            String groupHeaderName = StringUtil.getUUID() + ".jpg";
            groupHeaderFile = dir + groupHeaderName;
            filePath = groupheaderdir + datePath + groupHeaderName;
        } else {
            filePath = oldPath;
            groupHeaderFile = groupheaderroot + filePath;
        }
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] bytes = decoder.decodeBuffer(base64File);
        FileUtil.copyTo(bytes, groupHeaderFile);
        return filePath;
    }

    private static String saveBase64File(String base64File, String oldPath) throws IOException {
        String filePath = "";
        String root = Config.get("uploadpath");
        Date now = new Date();
        String dir = root + DateUtil.toString(now, "yyyy") + File.separator + DateUtil.toString(now, "MM") + File.separator + DateUtil.toString(now, "dd");
        File dirFile = new File(dir);
        if(!dirFile.exists()) {
            dirFile.mkdirs();
        }
        filePath = StringUtils.isEmpty(oldPath) || StringUtils.equals(oldPath, "\"\"") ? DateUtil.toString(now, "yyyy") + File.separator + DateUtil.toString(now, "MM") + File.separator + DateUtil.toString(now, "dd") + File.separator + StringUtil.getUUID() + ".jpg" : oldPath;
        String myfile = root + filePath;
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] bytes = decoder.decodeBuffer(base64File);
        FileUtil.copyTo(bytes, myfile);
        return filePath;
    }

    //保存实名认证审核图片
    public static final String savePicture(MultipartFile file, String oldFileName) throws IOException {
        String filePath = "";
        String root = Config.get("uploadpath");
        Date now = new Date();
        String dir = root + DateUtil.toString(now, "yyyy") + File.separator + DateUtil.toString(now, "MM") + File.separator + DateUtil.toString(now, "dd");
        File dirFile = new File(dir);
        if(!dirFile.exists()) {
            dirFile.mkdirs();
        }
        filePath = StringUtils.isEmpty(oldFileName) || StringUtils.equals(oldFileName, "\"\"") ? DateUtil.toString(now, "yyyy") + File.separator + DateUtil.toString(now, "MM") + File.separator + DateUtil.toString(now, "dd") + File.separator + StringUtil.getUUID() + "." + FileUtil.getFileExt(file.getOriginalFilename()) : oldFileName;
        String myfile = root + filePath;
        FileUtil.copyTo(file.getBytes(), myfile);
        return filePath;
    }

    public static String fileToBase64Str(String filePath) throws IOException {
        File file = new File(filePath);
        FileInputStream fis = FileUtils.openInputStream(file);
        Long len = file.length();
        byte[] bytes = new byte[len.intValue()];
        fis.read(bytes);
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(bytes);
    }

    public static void main(String[] args) throws IOException {
        String filePath = "C:\\Users\\nina\\Desktop\\user_log_b.png";
        System.out.println(fileToBase64Str(filePath));
    }
}
