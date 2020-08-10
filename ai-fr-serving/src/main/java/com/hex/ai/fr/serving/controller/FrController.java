package com.hex.ai.fr.serving.controller;

import com.alibaba.fastjson.JSONObject;
import com.hex.ai.commons.model.MessageResult;
import com.hex.ai.commons.model.OcrTask;
import com.hex.ai.fr.serving.conf.AiFrConf;
import com.hex.ai.fr.serving.model.TableRetResult;
import com.hex.ai.fr.serving.model.TaskRequest;
import com.hex.ai.fr.serving.model.TaskResponse;
import com.hex.ai.fr.serving.service.FrOcrService;
import com.hex.ai.fr.serving.service.FrService;
import com.hex.ai.fr.serving.util.FileZipUtils;
import com.hex.ai.fr.serving.util.ObjStorageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by zouwj on 2019-4-17.
 */
@RestController
public class FrController {
    public static final Logger logger = LoggerFactory.getLogger(FrController.class);

    @Autowired
    private FrService frService;

    @Autowired
    private FrOcrService frOcrService;

    @Autowired
    private AiFrConf aiFrConf;

//    @Autowired
//    private GatewayServing gatewayServing;

    @RequestMapping("/hex/ocrapi/fr")
    public MessageResult processFr(@RequestBody String req) {
        if(StringUtils.isEmpty(req)) {
            logger.error("FrController processFr req is Empty");
            return new MessageResult(false);
        }
        OcrTask modelConfig = null;
        try {
            JSONObject jsStr = JSONObject.parseObject(req);
            modelConfig = JSONObject.toJavaObject(jsStr, OcrTask.class);
        }catch (Exception e) {
            logger.error("FrController processFr req is Empty");
            return new MessageResult(false);
        }

        return this.frService.processFr(modelConfig);
    }

    /**
     * 永赢租赁发起识别接口
     * @param req
     * @return
     *
     */
    @RequestMapping("/hex/ocrapi/frocr")
    public TaskResponse processFrocr(@RequestBody String req) {
        logger.info("FrController processFrocr req is " + req);
        String waitprocess =aiFrConf.getBasePath()+System.getProperty("file.separator")+"waitprocess";
        String export =aiFrConf.getBasePath()+System.getProperty("file.separator")+"export";
        String temp =aiFrConf.getBasePath()+System.getProperty("file.separator")+"temp";
        String downtemp =aiFrConf.getBasePath()+System.getProperty("file.separator")+"downtemp";
        //初始化目录
        File waitprocessDir = new File(waitprocess);
        if(!waitprocessDir.exists()){
            File exportDir = new File(export);
            File tempDir = new File(temp);
            File downtempDir = new File(downtemp);
            waitprocessDir.mkdir();
            exportDir.mkdir();
            tempDir.mkdir();
            downtempDir.mkdir();
        }

        if(StringUtils.isEmpty(req)) {
            logger.error("FrController processFrocr req is Empty");
            return new TaskResponse("5",false,"OCR_PARAM_FAILED");
        }
        try {
            JSONObject jsStr = JSONObject.parseObject(req);
            TaskRequest taskRequest = JSONObject.toJavaObject(jsStr, TaskRequest.class);
            if(StringUtils.isEmpty(taskRequest.getBatchNo()) || StringUtils.isEmpty(taskRequest.getObjectId())){
                return new TaskResponse("5",false,"OCR_PARAM_FAILED",taskRequest.getObjectId());
            }
            String clientQueueName = "Client-" + UUID.randomUUID().toString().substring(0, 4);
            String batchPath = waitprocess + System.getProperty("file.separator")+ taskRequest.getBatchNo();
            String exportPath =export + System.getProperty("file.separator")+ taskRequest.getBatchNo();
            String tempPath = temp + System.getProperty("file.separator")+ taskRequest.getBatchNo();

            OcrTask modelConfig = new OcrTask();
            modelConfig.setOcrType("FINANCE_REPORT");
            modelConfig.setCity("FINANCE_REPORT");
            modelConfig.set_from(clientQueueName);
            modelConfig.set_id(UUID.randomUUID().toString());
            modelConfig.setBatchNo(taskRequest.getBatchNo());
            modelConfig.setPriority(2);
            modelConfig.setBatchPath(batchPath);
            modelConfig.setExportPath(exportPath);

            //根据objectId从对象存储下载
            String downPath = tempPath + ".zip";
            logger.info("download start...batchNo=["+taskRequest.getBatchNo()+"][" +aiFrConf.s3Admin+"] ["+aiFrConf.bucket+"] ["+aiFrConf.configPath+"]" );
            boolean downloadOk = ObjStorageUtil.download(taskRequest.getObjectId(),downPath,aiFrConf.s3Admin,aiFrConf.bucket,aiFrConf.configPath);
            logger.info("download end...batchNo=["+taskRequest.getBatchNo()+"]downloadOk[" +downloadOk+"]" );
            if(!downloadOk){
                return new TaskResponse("6",false,"OCR_OBJDOWN_FAILED",taskRequest.getObjectId());
            }

            //解压文件
            logger.info("start unzip file... batchNo=["+taskRequest.getBatchNo()+"]");
            unZipBatchNoDir(exportPath,taskRequest.getBatchNo());
            logger.info("end unzip file... batchNo=["+taskRequest.getBatchNo()+"]");

            //开始预处理图片
            logger.info("start sendOcrTable...batchNo=["+taskRequest.getBatchNo()+"]");
            String combinJson = this.frOcrService.sendOcrTable(modelConfig);
            logger.info("end sendOcrTable...batchNo=["+taskRequest.getBatchNo()+"]");
            if(StringUtils.isEmpty(combinJson)){
                logger.error("combinJson is null...");
                FileZipUtils.delFile(new File(batchPath));
                FileZipUtils.delFile(new File(exportPath));
                new TaskResponse("2",false,"OCR_PREPROCESS_FAILED",taskRequest.getObjectId());
            }

            //发起引擎识别
            logger.info("start sendOcrResult...batchNo=["+taskRequest.getBatchNo()+"]");
            String ocrResultData = this.frOcrService.sendOcrResult(modelConfig,aiFrConf.serverurl,aiFrConf.isBlueImage,combinJson);
            logger.info("end sendOcrResult...batchNo=["+taskRequest.getBatchNo()+"]");
            if(StringUtils.isEmpty(ocrResultData)){
                logger.error("ocrResultData is null...");
                FileZipUtils.delFile(new File(batchPath));
                FileZipUtils.delFile(new File(exportPath));
                return new TaskResponse("3",false,"OCR_RECOG_FAILED");
            }

            //把识别结果压缩打包
            logger.info("start zip...batchNo=["+taskRequest.getBatchNo()+"]");
            String tempZip = dozipBatchNoDir(taskRequest.getBatchNo());
            logger.info("end zip...batchNo=["+taskRequest.getBatchNo()+"]");

            //把文件上传到对象存储获取新的objectId
            logger.info("start upload file...batchNo=["+taskRequest.getBatchNo()+"]");
            String newObjectId = ObjStorageUtil.upload(aiFrConf.bucket,aiFrConf.s3Admin,tempZip,aiFrConf.configPath);
            logger.info("end upload file...batchNo=["+taskRequest.getBatchNo()+"] newObjectId=["+newObjectId+"]");

            //上传成功即删除文件
            FileZipUtils.delFile(new File(tempZip));
            FileZipUtils.delFile(new File(batchPath));
            FileZipUtils.delFile(new File(exportPath));

            if(StringUtils.isEmpty(newObjectId)){
                logger.error("upload file failed...batchNo=["+taskRequest.getBatchNo()+"] newObjectId=["+newObjectId+"]");
                return new TaskResponse("7",false,"OCR_OBJ_UPLOAD_FAILED",newObjectId);
            }

            logger.info(taskRequest.getBatchNo() + " finish ocrResult is ok...");
            return new TaskResponse("0",true,"SUCCESS",newObjectId);
        }catch (Exception e) {
            e.printStackTrace();
            logger.error("FrController processFrocr ocr failed..." +e.getMessage());
            return new TaskResponse("4",false,"REQUEST_OUTTIME");
        }
    }

    /**
     * 表格预处理独立接口
     * @param req
     * @return
     * @throws IOException
     */
    @RequestMapping("/hex/ocrapi/tabledetect")
    public TableRetResult ocrTableResult(@RequestBody String req) throws IOException{
        logger.info("start ocrTableResult...");
        TableRetResult messageResult = new TableRetResult(false);
        JSONObject jsStr = JSONObject.parseObject(req);
        Object base64 = jsStr.get("imgSrcContent");
        String base64Imag = null;
        if(StringUtils.isEmpty(base64)) {
            logger.error("imgSrcContent is null...");
            return new TableRetResult(false,"request paramter is null");
        }

        base64Imag = (String)base64;
        //新建批次目录
        String batchNo = getBatchNo();
        logger.info("ocrTableResult...batchNo=["+batchNo+"]");
        String batchPath =aiFrConf.getBasePath()+System.getProperty("file.separator") + "waitprocess" + System.getProperty("file.separator") + batchNo;
        String exportPath =aiFrConf.getBasePath()+System.getProperty("file.separator")+"export" + System.getProperty("file.separator") + batchNo;
        String formattedFile = batchPath + System.getProperty("file.separator") + "formatted" +  System.getProperty("file.separator") + batchNo.substring(0,5) + ".jpg";

        OcrTask modelConfig = new OcrTask();
        modelConfig.setBatchPath(batchPath);
        modelConfig.setExportPath(exportPath);
        modelConfig.setBatchNo(batchNo);
        //base64转图片
        boolean isOk = FileZipUtils.getFileFromBase64(base64Imag,batchPath+ System.getProperty("file.separator") + batchNo.substring(0,5) + ".jpg");
        if(!isOk) {
            logger.error("base64 change image failed...");
            FileZipUtils.delFile(new File(batchPath));
            FileZipUtils.delFile(new File(exportPath));
            return new TableRetResult(false,"base64 change image failed...");
        }
        //开始预处理图片
        logger.info("start sendOcrTable...batchNo=["+batchPath+"]");
        String combinJson = this.frOcrService.sendOcrTable(modelConfig);
        logger.info("end sendOcrTable...batchNo=["+batchPath+"]");
        if(StringUtils.isEmpty(combinJson)){
            logger.error("ocrTableResult ocr failed...");
            FileZipUtils.delFile(new File(batchPath));
            FileZipUtils.delFile(new File(exportPath));
            return new TableRetResult(false,"ocrTableResult ocr failed...");
        }
        logger.info("end ocrTableResult successful...batchNo=["+batchPath+"]");
        //预处理后的图片转base64
        String base64Image = FileZipUtils.getBase64FromFile(new File(formattedFile));

        FileZipUtils.delFile(new File(batchPath));
        FileZipUtils.delFile(new File(exportPath));

        //返回json结果和纠偏图像
        messageResult.setImage(base64Image);
        messageResult.setTable(JSONObject.parseObject(combinJson));
        messageResult.setStatus(true);
        messageResult.setMessage("success");
        messageResult.setCode("0");

        return messageResult;
    }

    @RequestMapping("/hex/ocrapi/getBaseStr")
    public TableRetResult getBaseStr(@RequestBody String req) throws IOException{
        TableRetResult messageResult = new TableRetResult();
        String batchPath =aiFrConf.getBasePath()+System.getProperty("file.separator") + "waitprocess" + System.getProperty("file.separator") + "test1.jpg";
        //预处理后的图片转base64
        String base64Image = FileZipUtils.getBase64FromFile(new File(batchPath));
        messageResult.setImage(base64Image);
        messageResult.setStatus(true);
        messageResult.setCode("0");
        //返回json结果和纠偏图像
        return messageResult;
    }

    public String dozipBatchNoDir(String batchNo) throws IOException {
        String tempDir = aiFrConf.basePath + System.getProperty("file.separator") + "temp" + System.getProperty("file.separator") + batchNo ;
        File tempBatchSource = new File(tempDir);
        if(!tempBatchSource.exists()){
            tempBatchSource.mkdir();
        }
        String exportPath = aiFrConf.basePath + System.getProperty("file.separator") + "export" + System.getProperty("file.separator") + batchNo + System.getProperty("file.separator");
        String waitprocessPath = aiFrConf.basePath + System.getProperty("file.separator") + "waitprocess" + System.getProperty("file.separator")+ batchNo + System.getProperty("file.separator");
        FileZipUtils.doZip(exportPath,tempDir + System.getProperty("file.separator") + "export.zip");
        FileZipUtils.doZip(waitprocessPath,tempDir + System.getProperty("file.separator") + "waitprocess.zip");
        FileZipUtils.doZip(tempDir,tempDir + ".zip");

        FileZipUtils.delFile(tempBatchSource);
        return tempDir + ".zip";
    }

    public String unZipBatchNoDir(String exportPath,String batchNo) throws IOException{
        File exportDir = new File(exportPath);
        if(!exportDir.exists()){
            exportDir.mkdir();
        }
        String tempDir = aiFrConf.basePath + System.getProperty("file.separator")+"temp"+ System.getProperty("file.separator") + batchNo ;
        String waitprocessPath = aiFrConf.basePath + System.getProperty("file.separator") + "waitprocess" + System.getProperty("file.separator");
        FileZipUtils.unZipFiles(tempDir + ".zip",waitprocessPath);
        FileZipUtils.delFile(new File(tempDir));
        FileZipUtils.delFile(new File(tempDir+".zip"));

        return tempDir + ".zip";
    }

    /**
     * 随机生成批次目录
     * @return
     */
    private String getBatchNo(){
        String batchNo = (int)((Math.random()*9+1)*Math.pow(10,7))+"";
        //新建目录
        String waitprocess =aiFrConf.getBasePath()+System.getProperty("file.separator")+"waitprocess";
        String export =aiFrConf.getBasePath()+System.getProperty("file.separator")+"export";
        //初始化目录
        File waitprocessDir = new File(waitprocess);
        if(!waitprocessDir.exists()){
            File exportDir = new File(export);
            waitprocessDir.mkdir();
            exportDir.mkdir();
        }

        //创建批次目录
        String batchPath = waitprocess + System.getProperty("file.separator")+ batchNo;
        String exportPath =export + System.getProperty("file.separator")+ batchNo;
        String formattedPath =batchPath + System.getProperty("file.separator")  + System.getProperty("file.separator") + "formatted";
        File batchFile = new File(batchPath);
        File exportFile = new File(exportPath);
        File formattedFile = new File(formattedPath);
        if(!batchFile.exists()){
            batchFile.mkdir();
            exportFile.mkdir();
            formattedFile.mkdir();
        }

        return batchNo;
    }
}
