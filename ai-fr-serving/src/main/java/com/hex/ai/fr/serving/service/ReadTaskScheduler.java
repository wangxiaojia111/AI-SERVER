package com.hex.ai.fr.serving.service;
//
//import com.alibaba.fastjson.JSONObject;
//import com.hex.ai.commons.model.MessageResult;
//import com.hex.ai.commons.model.OcrTask;
//import com.hex.ai.commons.model.ReadOcrTaskRequest;
//import com.hex.ai.commons.util.AIConstants;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//@Component
public class ReadTaskScheduler {
//    private final Logger logger = LoggerFactory.getLogger(this.getClass());
//    @Autowired
//    private GatewayServing gatewayServing;
//
//    @Autowired
//    private FrService frService;
//
//    @Scheduled(fixedRate=500)
//    public void getFrTasks() {
//
//        if(!frService.getTaskScan()){
//            return;
//        }
//
//        if (frService.getRunningTaskNum() <= 0) {
//            return;
//        }
//
//        MessageResult messageResult = gatewayServing.getTask(new ReadOcrTaskRequest(AIConstants.MODULE_FR,true));
//        if (messageResult != null && messageResult.isStatus()) {
//            try {
//                JSONObject jsStr = JSONObject.parseObject(JSONObject.toJSONString(messageResult.getData()));
//                OcrTask modelConfig = JSONObject.toJavaObject(jsStr, OcrTask.class);
//
//                this.frService.processFr(modelConfig);
//            } catch (Exception e) {
//                logger.error("getFrTasks processFr exception [" + e.getMessage() + "]");
//            }
//        }
//    }
}
