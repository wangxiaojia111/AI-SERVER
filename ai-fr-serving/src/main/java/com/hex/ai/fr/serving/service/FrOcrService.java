package com.hex.ai.fr.serving.service;

import com.alibaba.fastjson.JSONObject;
import com.hex.ai.commons.model.OcrTask;
import com.hex.ai.commons.util.HttpClientUtil;
import com.hex.ai.fr.serving.util.ChangeJsonUtil;
import com.hex.ai.fr.serving.util.FrServerUtil;
import com.hex.commons.heximage.ImageProcessor;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class FrOcrService {

    public static final Logger logger = LoggerFactory.getLogger(FrOcrService.class);


    /**
     * 预处理表格json坐标
     * @param modelConfig
     * @return
     */
    public String sendOcrTable(OcrTask modelConfig) {
        logger.info("开始识别表格流水["+modelConfig.getBatchNo()+"]");
        long l1 =  System.currentTimeMillis();
        try {
            ImageProcessor.getTableDetect(modelConfig.getBatchNo(), modelConfig.getBatchPath(), modelConfig.getExportPath(),1,0);
            long l2 =  System.currentTimeMillis();
            long l = l2-l1;
            logger.info("流水["+modelConfig.getBatchNo()+"]获取表格时长"+l+"(ms)");
            JSONObject combinJson = ChangeJsonUtil.changeJson(modelConfig);
            if (combinJson == null) {
                logger.error("getTableDetect combinJson = null");
                FrServerUtil.writeOcrFail(modelConfig.getExportPath(),modelConfig.getBatchNo());
            }
            logger.info("表格预处理完成["+modelConfig.getBatchNo()+"]");
            return combinJson.toJSONString();
        }catch (Exception e){
            logger.error("processFr exception["+e.getMessage()+"]");
            FrServerUtil.writeOcrFail(modelConfig.getExportPath(),modelConfig.getBatchNo());
        }
        return null;
    }


    /**
     * 根据预处理的json坐标发起识别
     * @param modelConfig  必要的批次参数
     * @param ocrUrl   识别引擎url
     * @param isBlueImage 默认true
     * @param combinJson  预处理json结果
     * @return
     */
    public String sendOcrResult(OcrTask modelConfig, String ocrUrl, boolean isBlueImage, String combinJson) {
        logger.info("开始发起财报引擎识别["+modelConfig.getBatchNo()+"]");
        long l2 =  System.currentTimeMillis();
        try {
            String saveFileName = "";
            saveFileName = modelConfig.getExportPath() + "/" + modelConfig.getBatchNo() + ".tmp";
            FileUtils.writeStringToFile(new File(saveFileName), combinJson, "gbk");
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
                    return null;
                }

                files.clear();
                files.add(new File(zipFileName));
            }

            String responseContent = HttpClientUtil.getInstance().sendFrPost(ocrUrl, new File(saveFileName), files);
            long l4 =  System.currentTimeMillis();
            long l = l4-l2;
            logger.info("流水["+modelConfig.getBatchNo()+"]识别时长="+l+"(毫秒)");
            if(StringUtils.isEmpty(responseContent)){
                logger.error("processFr responseContent is null");
                FrServerUtil.writeOcrFail(modelConfig.getExportPath(),modelConfig.getBatchNo());
                return null;
            }
            JSONObject resObject = JSONObject.parseObject(responseContent);

            saveFileName = saveFileName.substring(0, saveFileName.length() - 3) + "json";
            FileUtils.writeStringToFile(new File(saveFileName), resObject.get("data").toString(), "gbk");
            String corpFolder = modelConfig.getBatchPath() + "/crop/";
            FileUtils.forceMkdir(new File(corpFolder));
            ImageProcessor.cutFrImage(modelConfig.getBatchPath() + "/formatted/",saveFileName,corpFolder);
            long l5 =  System.currentTimeMillis();
            logger.info("财报识别["+modelConfig.getBatchNo()+"]完成，总用时="+l+"(毫秒)，isBlueImage="+isBlueImage);

            if(isBlueImage){
                String blurPath = modelConfig.getBatchPath() + "/blur/";
                FileUtils.forceMkdir(new File(blurPath));
                ImageProcessor.reduceBlurImage(modelConfig.getBatchPath() + "/formatted/",blurPath,30);
            }
            long l6 =  System.currentTimeMillis();
            l = l6-l5;
            logger.info("模糊图片用时["+l+"]完成");
            return resObject.get("data").toString();
        }catch (Exception e){
            logger.error("processFr exception["+e.getMessage()+"]");
            FrServerUtil.writeOcrFail(modelConfig.getExportPath(),modelConfig.getBatchNo());
        }
        return null;
    }
}
