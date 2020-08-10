package com.hex.ai.fr.serving.util;


//import com.amazonaws.util.Base64;

import com.alibaba.fastjson.JSONObject;
import com.topcheer.storage.s3.info.ObjectCreateInfo;
import com.topcheer.storage.s3.info.ObjectCreateResult;
import com.topcheer.storage.s3.info.ObjectDownloadInfo;
import com.topcheer.storage.s3.info.ObjectDownloadResult;
import com.topcheer.storage.s3.service.S3Admin;
import com.topcheer.storage.s3.service.S3ServiceFoctory;
import com.topcheer.storage.util.ConfigUtil;

import java.util.HashMap;
import java.util.Map;


public class ObjStorageUtil {

	/**
	 * 接口1 上传对象
	 * @throws Exception
	 *
	 */
	public static String upload(String bucketName,String s3Admin,String filePath, String configPath) throws Exception {
//		ConfigUtil.setConfigPath("C:\\Users\\pub5012\\Desktop\\config\\s3storage.xml");
//      S3Admin s3 = S3ServiceFoctory.getS3Admin("fileTransfer");
// 		info.setBucketName("ocr");
		ConfigUtil.setConfigPath(configPath);
		S3Admin s3 = S3ServiceFoctory.getS3Admin(s3Admin);
		ObjectCreateInfo info = new ObjectCreateInfo();
		/**
		 * 上传到哪个bucket
		 */
		info.setBucketName(bucketName);

		/**
		 * 要上传的本地文件
		 */
		info.setFilePath(filePath);
		/**
		 * 调用上传对象的接口
		 */
		ObjectCreateResult result = s3.createObject(info);
		/**
		 * 上传的对象的唯一编号，你要自己妥善保存哦，后续要拿这个编号下载
		 */
		System.out.println("new created object id :" + result.getObjectId());
		return  result.getObjectId();
	}

	/**
	 * 接口2 下载对象
	 * @throws Exception
	 */
	public static Boolean download(String objectId,String savePathFile,String s3Admin,String bucketName,String configPath) throws Exception {
		System.out.println("start download objectId =[" + objectId+"]");
		ConfigUtil.setConfigPath(configPath);
		S3Admin s3 = S3ServiceFoctory.getS3Admin(s3Admin);
		ObjectDownloadResult result1 = s3.downloadObject(new ObjectDownloadInfo(bucketName,
				objectId,savePathFile));
		System.out.println("finish download =[" +result1.isResult()+"]");
		return result1.isResult();
	}

	public static void main(String[] args) throws Exception {
		Map<String,Object> paramMap = new HashMap<>();
		paramMap.put("uuid","sss");
		paramMap.put("uploadResult",true);
		paramMap.put("ocrResult","sss");
		String ocrResult = JSONObject.toJSONString(paramMap);
//        sendReportResult("http://12.99.133.164:8181/FLS-MW/OcrIdentify/Identify.do",ocrResult);
//		upload();
//		download();
	}


}
