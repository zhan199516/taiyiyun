package com.taiyiyun.passport.util;

import java.io.File;
import net.coobird.thumbnailator.Thumbnails;

public class ImageUtil {
	
	public static void zoomImage(File srcFile, File targetFile, double scale) throws Exception {
		Thumbnails.of(srcFile).scale(scale).toFile(targetFile);
	}
	
	public static void resizeImage(File srcFile, File targetFile, int width, int height) throws Exception {
		Thumbnails.of(srcFile).size(width, height).toFile(targetFile);
	}

}
