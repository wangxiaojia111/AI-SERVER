package com.hex.ai.fr.serving.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@ConfigurationProperties(prefix = "task")
public class AiFrConf {


    public String serverurl;


    public boolean taskScan;


    public int processors;


    public boolean isBlueImage;

    public static int threadNum;

    public String basePath;

    public String s3Admin;

    public String bucket;

    public String configPath;

    public String getS3Admin() {
        return s3Admin;
    }

    public void setS3Admin(String s3Admin) {
        this.s3Admin = s3Admin;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getConfigPath() {
        return configPath;
    }

    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getServerurl() {
        return serverurl;
    }

    public void setServerurl(String serverurl) {
        this.serverurl = serverurl;
    }

    public boolean isTaskScan() {
        return taskScan;
    }

    public void setTaskScan(boolean taskScan) {
        this.taskScan = taskScan;
    }

    public int getProcessors() {
        return processors;
    }

    public void setProcessors(int processors) {
        this.processors = processors;
    }

    public boolean isBlueImage() {
        return isBlueImage;
    }

    public void setBlueImage(boolean blueImage) {
        isBlueImage = blueImage;
    }

    public static int getThreadNum() {
        return threadNum;
    }

    public static void setThreadNum(int threadNum) {
        AiFrConf.threadNum = threadNum;
    }
}
