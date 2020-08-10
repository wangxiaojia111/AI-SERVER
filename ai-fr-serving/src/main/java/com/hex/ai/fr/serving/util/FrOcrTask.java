package com.hex.ai.fr.serving.util;

import com.alibaba.fastjson.JSONObject;
import com.hex.ai.commons.model.OcrTask;
import com.hex.ai.commons.util.HttpClientUtil;
import com.hex.commons.heximage.ImageProcessor;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FrOcrTask implements Runnable {
    public static final Logger logger = LoggerFactory.getLogger(FrOcrTask.class);
    private OcrTask modelConfig = null;
    private String ocrUrl;
    private boolean isBlueImage;

    public FrOcrTask(OcrTask modelConfig,String url,boolean isBlueImage){
        this.modelConfig = modelConfig;
        this.ocrUrl = url;
        this.isBlueImage = isBlueImage;
    }

    @Override
    public void run() {
        logger.info("开始识别财报["+modelConfig.getBatchNo()+"]");
        long l1 =  System.currentTimeMillis();
        try {
            String saveFileName = "";
            ImageProcessor.getTableDetect(modelConfig.getBatchNo(), modelConfig.getBatchPath(), modelConfig.getExportPath(),1,0);
            long l2 =  System.currentTimeMillis();
            long l = l2-l1;
            logger.info("流水["+modelConfig.getBatchNo()+"]获取表格时长"+l+"(ms)");
            JSONObject combinJson = ChangeJsonUtil.changeJson(modelConfig);
            if (combinJson == null) {
                logger.error("getTableDetect combinJson = null");
                FrServerUtil.writeOcrFail(modelConfig.getExportPath(),modelConfig.getBatchNo());
                return;
            }

            saveFileName = modelConfig.getExportPath() + "/" + modelConfig.getBatchNo() + ".tmp";
            FileUtils.writeStringToFile(new File(saveFileName), combinJson.toJSONString(), "gbk");
            List<File> files = new ArrayList<>();
            File file = new File(modelConfig.getBatchPath() + "/formatted/");
            File[] fileList = file.listFiles();
            for (int m = 0; m < fileList.length; m++) {
                if (fileList[m].isFile()) {//判断是否为文件
                    if (fileList[m].getName().endsWith(".jpg")) {
                        files.add(fileList[m]);
                    }
                }
            }
            logger.info("流水["+modelConfig.getBatchNo()+"]文件数["+files.size()+"]");
            if(files.size() > 1){
                String zipFileName = modelConfig.getBatchPath()+"/"+modelConfig.getBatchNo()+".zip";
                try {
                    FrServerUtil.doZip(modelConfig.getBatchPath() + "/formatted/",zipFileName);
                }catch (IOException e){
                    FrServerUtil.writeOcrFail(modelConfig.getExportPath(),modelConfig.getBatchNo());
                    return;
                }

                files.clear();
                files.add(new File(zipFileName));
            }

            String responseContent = HttpClientUtil.getInstance().sendFrPost(ocrUrl, new File(saveFileName), files);
            long l4 =  System.currentTimeMillis();
            l = l4-l2;
            logger.info("流水["+modelConfig.getBatchNo()+"]识别时长="+l+"(毫秒)");
            if(StringUtils.isEmpty(responseContent)){
                logger.error("processFr responseContent is null");
                FrServerUtil.writeOcrFail(modelConfig.getExportPath(),modelConfig.getBatchNo());
                return;
            }
            JSONObject resObject = JSONObject.parseObject(responseContent);

            saveFileName = saveFileName.substring(0, saveFileName.length() - 3) + "json";
            FileUtils.writeStringToFile(new File(saveFileName), resObject.get("data").toString(), "gbk");
            String corpFolder = modelConfig.getBatchPath() + "/crop/";
            FileUtils.forceMkdir(new File(corpFolder));
            ImageProcessor.cutFrImage(modelConfig.getBatchPath() + "/formatted/",saveFileName,corpFolder);
            long l5 =  System.currentTimeMillis();
            l = l4-l1;
            logger.info("财报识别["+modelConfig.getBatchNo()+"]完成，总用时="+l+"(毫秒)");

            logger.info("财报识别["+modelConfig.getBatchNo()+"]完成，isBlueImage="+isBlueImage);
            if(isBlueImage){
                String blurPath = modelConfig.getBatchPath() + "/blur/";
                FileUtils.forceMkdir(new File(blurPath));
                ImageProcessor.reduceBlurImage(modelConfig.getBatchPath() + "/formatted/",blurPath,30);
            }
            long l6 =  System.currentTimeMillis();
            l = l6-l5;
            logger.info("模糊图片用时["+l+"]完成");
        }catch (Exception e){
            logger.error("processFr exception["+e.getMessage()+"]");
            FrServerUtil.writeOcrFail(modelConfig.getExportPath(),modelConfig.getBatchNo());
        }
    }
}
