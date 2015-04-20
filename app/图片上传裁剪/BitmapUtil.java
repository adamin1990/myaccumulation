package com.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.widget.Toast;

import com.bitcare.patient.data.Constants;

/**
 * 图片处理
 */

@EBean
public class BitmapUtil {
	@RootContext
	Context context;
	BitMapCallBack bitMapCallBack;

	private void callback(int requestCode, String path) {
		if (bitMapCallBack != null) {
			bitMapCallBack.callBack(requestCode, path);
		}
	}

	/**
	 * 私有的保存图片
	 * @param source
	 * @param filePath
	 * @param fileName
	 */
	private String saveBitmap_p(Bitmap source, String filePath) {
		if (checkSd()) {
			File file = new File(filePath);
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			if (file.exists()) {
				file.delete();
			}
			try {
				file.createNewFile();
				FileOutputStream fileOut = new FileOutputStream(file);
				source.compress(Bitmap.CompressFormat.PNG, 100, fileOut);
				fileOut.flush();
				fileOut.close();
				fileOut = null;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return filePath;
		} else {
			Toast.makeText(context, "SD卡不存在", Toast.LENGTH_LONG).show();
			return null;
		}
	}

	/* —————————————————————————————————————————————————————————— */
	public void setCallBack(BitMapCallBack bitMapCallBack) {
		this.bitMapCallBack = bitMapCallBack;
	}

	/**
	 * 返回一个已时间为名字的图片名
	 * @return
	 */
	public String getNewFileName() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		return sdf.format(new java.util.Date()) + ".png";
	}

	/**
	 * 根目录
	 * @return
	 */
	public String getSDcardPath() {
		return Environment.getExternalStorageDirectory().getAbsoluteFile().toString();
	}

	/**
	 * 检查是否有SD卡
	 * @return
	 */
	public boolean checkSd() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ? true : false;
	}

	/**
	 * 保存图片
	 * @param source
	 * @param filePath
	 * @param fileName
	 */
	@Background
	public void saveBitmap(int requestCode, Bitmap source, String filePath) {
		callback(requestCode, saveBitmap_p(source, filePath));
	}

	/**
	 * 获得图片
	 * @param filePath
	 * @return
	 */
	public Bitmap getBitmap(String filePath) {
		return BitmapFactory.decodeFile(filePath);
	}

	/**
	 * 保存缩略图
	 * @param bitmap
	 * @param fileName
	 * @param width
	 * @param height
	 * @return
	 */
	// public String saveThumbnail(Bitmap bitmap, String fileName, int width,
	// int height) {
	// Bitmap thumbanilBitma = ThumbnailUtils.extractThumbnail(bitmap, width,
	// height);
	// saveBitmap(thumbanilBitma,
	// Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
	// Constants.LOCAL_THUMBNAIL_DIR, fileName);
	// thumbanilBitma.recycle();
	// thumbanilBitma = null;
	// return Constants.LOCAL_THUMBNAIL_DIR + "/" + fileName;
	// }

	/**
	 * 保存缩略图
	 * @param filePath
	 * @param fileName
	 * @param width
	 * @param height
	 * @return
	 */
	@Background
	public void saveThumbnail(int requestCode, String filePath, int width, int height) {
		Bitmap sourceBitmap = getBitmap(filePath);
		Bitmap thumbanilBitmap = ThumbnailUtils.extractThumbnail(sourceBitmap, width, height);
		String thumbnailFileName = getNewFileName();
		String absloutePath = saveBitmap_p(thumbanilBitmap, getSDcardPath() + "/" + Constants.LOCAL_THUMBNAIL_DIR + thumbnailFileName);
		thumbanilBitmap.recycle();
		thumbanilBitmap = null;
		sourceBitmap.recycle();
		sourceBitmap = null;
		callback(requestCode, absloutePath);
	}

	/**
	 * 保存为黑白照片
	 * @param filePath
	 * @return
	 */
	@Background
	public void saveBlackAndWhite(int requestCode, String filePath) {
		Bitmap bm = getBitmap(filePath);
		int width, height;
		height = bm.getHeight();
		width = bm.getWidth();
		Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		Canvas c = new Canvas(bmpGrayscale);
		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);
		ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(f);
		c.drawBitmap(bm, 0, 0, paint);
		bm.recycle();
		bm = null;
		String fp = saveBitmap_p(bmpGrayscale, getSDcardPath() + "/" + Constants.LOCAL_PHOTO_DIR + getNewFileName());
		bmpGrayscale.recycle();
		bmpGrayscale = null;
		callback(requestCode, fp);
	}

	/**
	 * 旋转图片
	 * @param path 图片绝对路径
	 * @return 图片的旋转角度
	 */
	@Background
	public void rotateBitmapDegree(int requestCode, String path) {
		int degree = 0;
		try {
			// 从指定路径下读取图片，并获取其EXIF信息
			ExifInterface exifInterface = new ExifInterface(path);
			// 获取图片的旋转信息
			int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
			if (degree != 0) {
				rotateBitmapByDegree(requestCode, path, degree);
			} else {
				callback(requestCode, path);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 将图片按照某个角度进行旋转
	 * @param bm 需要旋转的图片
	 * @param degree 旋转角度
	 * @return 旋转后的图片
	 */
	public void rotateBitmapByDegree(int requestCode, String filePath, int degree) {
		Bitmap sourceBitmap = getBitmap(filePath);
		Bitmap returnBm = null;
		// 根据旋转角度，生成旋转矩阵
		Matrix matrix = new Matrix();
		matrix.postRotate(degree);
		// 将原始图片按照旋转矩阵进行旋转，并得到新的图片
		returnBm = Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight(), matrix, true);
		sourceBitmap.recycle();
		sourceBitmap = null;
		String fp = saveBitmap_p(returnBm, getSDcardPath() + "/" + Constants.LOCAL_PHOTO_DIR + getNewFileName());
		callback(requestCode, fp);
	}

	public interface BitMapCallBack {
		void callBack(int requestCode, String path);
	}
}