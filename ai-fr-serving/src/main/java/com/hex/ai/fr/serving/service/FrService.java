package com.hex.ai.fr.serving.service;

import com.hex.ai.commons.model.MessageResult;
import com.hex.ai.commons.model.OcrTask;
import com.hex.ai.fr.serving.conf.AiFrConf;
import com.hex.ai.fr.serving.util.FrOcrTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class FrService {
    public static final Logger logger = LoggerFactory.getLogger(FrService.class);

    @Autowired
    private AiFrConf aiFrConf;
    private static int cpuProcessors  = 0;//CPU核数

    //创建线程池
    public static ExecutorService executorService = null;
    public static ExecutorService getExecutorService() {
        cpuProcessors = AiFrConf.threadNum;
//        cpuProcessors = Runtime.getRuntime().availableProcessors();
        if (executorService == null) {
            logger.info("线程数="+cpuProcessors);
            executorService = Executors.newFixedThreadPool(cpuProcessors);
        }

        return executorService;
    }

    public boolean getTaskScan(){
        return this.aiFrConf.taskScan;
    }

    public MessageResult processFr(OcrTask modelConfig) {
        getRunningTaskNum();
        getExecutorService().submit(new FrOcrTask(modelConfig,aiFrConf.serverurl,aiFrConf.isBlueImage));
        return new MessageResult(true);
    }

    public boolean processFrocr(OcrTask modelConfig) {
        boolean result = false;
        getRunningTaskNum();
        getExecutorService().submit(new FrOcrTask(modelConfig,aiFrConf.serverurl,aiFrConf.isBlueImage));
        result = true;
        return result;
    }

    public  int getRunningTaskNum(){
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor )getExecutorService();
        int count =  cpuProcessors - threadPoolExecutor.getActiveCount();
        return count;
    }
}
