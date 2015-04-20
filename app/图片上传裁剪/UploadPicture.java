package com.util;

import java.io.File;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.bitcare.patient.util.BitmapUtil.BitMapCallBack;

/**
 * 图片上传
 */
@EBean
public class UploadPicture {
	@RootContext
	Context context;
	private Uri photoUri;// 拍摄或选择的原图
	private int requestCode;
	private boolean isCrop;// 是否需要裁剪
	private Uri img;// 选择并裁剪后的图
	private File fileCrop;// 裁剪后的图片文件
	private int aspectX; // 裁剪框比例
	private int aspectY; // 裁剪框比例
	private int outputX;// 输出图片大小
	private int outputY;// 输出图片大小
	private UploadCallBack callback;// 图片回调
	@Bean
	BitmapUtil bitmapUtil;

	ProgressDialog dialog;

	@AfterInject
	void init() {
		dialog = new ProgressDialog(context);
		dialog.setCancelable(false);
		dialog.setMessage("数据加载中……");
	}

	/**
	 * 回调
	 * @param callBack
	 */
	public void setUploadCallBack(UploadCallBack callBack) {
		this.callback = callBack;
	}

	/**
	 * 上传原图(注意预留requestCode)
	 * @param requestCode 拍摄为n,选择图片n+1
	 */
	public void uploadPictue(final int requestCode) {
		bitmapUtil.setCallBack(bitMapCallBack);
		this.requestCode = requestCode;
		isCrop = false;
		new AlertDialog.//
		Builder(context).//
				setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, android.R.id.text1, //
						new String[] { "拍摄照片", "选择图片" }), //
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								switch (which) {
								case 0:
									filming(requestCode);
									break;
								case 1:
									choosePhoto(requestCode + 1);
									break;
								}
							}
						}).show();
	}

	/**
	 * 选择图片并裁剪(注意预留requestCode)
	 * @param requestCode 拍摄为n,选择图片并裁剪n+1,拍摄裁剪n+2
	 * @param file 裁剪后的图片文件,文件地址文件可以为空
	 * @param aspectX 裁剪框比例
	 * @param aspectY 裁剪框比例
	 * @param outputX 输出图片大小
	 * @param outputY 输出图片大小
	 */
	public void uploadPictueByCrop(final int requestCode, final File file, final int aspectX, final int aspectY, final int outputX, final int outputY) {
		if (bitmapUtil.checkSd()) {
			bitmapUtil.setCallBack(bitMapCallBack);
			this.requestCode = requestCode;
			isCrop = true;
			this.fileCrop = file;
			if (!this.fileCrop.getParentFile().exists()) {
				this.fileCrop.getParentFile().mkdirs();
			}
			this.aspectX = aspectX;
			this.aspectY = aspectY;
			this.outputX = outputX;
			this.outputY = outputY;
			new AlertDialog.//
			Builder(context).//
					setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, android.R.id.text1, //
							new String[] { "拍摄照片", "选择图片" }), //
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									switch (which) {
									case 0:
										filming(requestCode);
										break;
									case 1:
										choosePhotoByCrop(requestCode + 1, aspectX, aspectY, outputX, outputY);
										break;
									}
								}
							}).show();
		} else {
			Toast.makeText(context, "内存卡不存在", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 返回结果
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		String filePath = null;
		if (resultCode == Activity.RESULT_OK) {
			if (this.requestCode == requestCode) {// 拍摄的图片
				dialog.show();
				filePath = CommUtil.getImageAbsolutePath((Activity) context, photoUri);
				bitmapUtil.rotateBitmapDegree(requestCode, filePath);
			} else if (this.requestCode + 1 == requestCode) {// 选择的图片或裁剪
				if (!isCrop) {// 选择原图
					if (data == null || data.getData() == null) {
						Toast.makeText(context, "选择图片文件出错", Toast.LENGTH_SHORT).show();
					} else {
						photoUri = data.getData();
						filePath = CommUtil.getImageAbsolutePath((Activity) context, photoUri);
					}
				} else {// 裁剪的图
					filePath = CommUtil.getImageAbsolutePath((Activity) context, img);
				}
				if (StringUtil.isNotTrimBlank(filePath)) {
					dialog.show();
					bitmapUtil.rotateBitmapDegree(requestCode, filePath);
				} else {
					Toast.makeText(context, "选择图片文件出错", Toast.LENGTH_SHORT).show();
				}
			} else if (this.requestCode + 2 == requestCode) {// 拍摄并裁剪的图
				filePath = CommUtil.getImageAbsolutePath((Activity) context, img);
				if (StringUtil.isNotTrimBlank(filePath)) {
					dialog.show();
					bitmapUtil.rotateBitmapDegree(requestCode, filePath);
				} else {
					Toast.makeText(context, "选择图片文件出错", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	public interface UploadCallBack {
		void callback(int requestCode, String filePath);
	}

	/* —————————————————————————————————————————————————————————— */
	private BitMapCallBack bitMapCallBack = new BitMapCallBack() {
		@Override
		public void callBack(int requestCode, String path) {
			dialog.dismiss();
			// 拍摄的图片
			if (UploadPicture.this.requestCode == requestCode) {
				if (isCrop) {// 需要裁剪
					photoUri = Uri.fromFile(new File(path));
					cropPhoto(requestCode + 2, aspectX, aspectY, outputX, outputY);
				}
			} else {
				if (callback != null) {
					callback.callback(requestCode, path);
				}
			}
		}
	};

	/**
	 * 拍摄图片
	 * @param requestCode
	 */
	private void filming(int requestCode) {
		// 拍照
		// 执行拍照前，应该先判断SD卡是否存在
		if (bitmapUtil.checkSd()) {
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			/***
			 * 需要说明一下，以下操作使用照相机拍照，拍照后的图片会存放在相册中的 这里使用的这种方式有一个好处就是获取的图片是拍照后的原图
			 * 如果不实用ContentValues存放照片路径的话 ，拍照后获取的图片为缩略图不清晰
			 */
			photoUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
			intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri);
			if (context instanceof Activity) {
				((Activity) context).startActivityForResult(intent, requestCode);
			} else {
				Toast.makeText(context, "不能使用非Activity的子类", Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(context, "内存卡不存在", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 选择图片
	 * @param requestCode
	 */
	private void choosePhoto(int requestCode) {
		// 选择图库
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		if (context instanceof Activity) {
			((Activity) context).startActivityForResult(intent, requestCode);
		} else {
			Toast.makeText(context, "不能使用非Activity的子类", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 选择图片并裁剪保存
	 * @param requestCode
	 * @param file 文件地址（必须有此文件）
	 * @param aspectX 裁剪框比例
	 * @param aspectY 裁剪框比例
	 * @param outputX 输出图片大小
	 * @param outputY 输出图片大小
	 */
	private void choosePhotoByCrop(int requestCode, int aspectX, int aspectY, int outputX, int outputY) {
		img = Uri.fromFile(fileCrop);
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
		intent.setType("image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", aspectX);// 裁剪框比例
		intent.putExtra("aspectY", aspectY);
		intent.putExtra("outputX", outputX);// 输出图片大小
		intent.putExtra("outputY", outputY);
		intent.putExtra("scale", true);// 如果选择的图小于裁剪大小则进行放大
		intent.putExtra("scaleUpIfNeeded", true);// 如果选择的图小于裁剪大小则进行放大
		intent.putExtra("return-data", false);// 是否输出bitmap
		intent.putExtra(MediaStore.EXTRA_OUTPUT, img);// 需要直接输出到文件的URI
		intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());// 输出格式
		if (context instanceof Activity) {
			((Activity) context).startActivityForResult(intent, requestCode);
		} else {
			Toast.makeText(context, "不能使用非Activity的子类", Toast.LENGTH_SHORT).show();
		}
		// 大图可以考虑直接写入文件而不返回bitmap
	}

	/**
	 * 裁剪拍摄出来的图片
	 * @param requestCode
	 * @param file 文件地址（必须有此文件）
	 * @param aspectX 裁剪框比例
	 * @param aspectY 裁剪框比例
	 * @param outputX 输出图片大小
	 * @param outputY 输出图片大小
	 */
	private void cropPhoto(int requestCode, int aspectX, int aspectY, int outputX, int outputY) {
		img = Uri.fromFile(fileCrop);
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(photoUri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", aspectX);
		intent.putExtra("aspectY", aspectY);
		intent.putExtra("outputX", outputX);
		intent.putExtra("outputY", outputY);
		intent.putExtra("scale", true);// 如果选择的图小于裁剪大小则进行放大
		intent.putExtra("scaleUpIfNeeded", true);// 如果选择的图小于裁剪大小则进行放大
		intent.putExtra(MediaStore.EXTRA_OUTPUT, img);
		intent.putExtra("return-data", false);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
		intent.putExtra("noFaceDetection", true); // no face detection
		((Activity) context).startActivityForResult(intent, requestCode);
	}
}
