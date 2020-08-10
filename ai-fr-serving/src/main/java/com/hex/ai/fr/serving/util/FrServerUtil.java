package com.hex.ai.fr.serving.util;

import com.hex.ai.fr.serving.controller.FrController;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FrServerUtil {
    public static final Logger logger = LoggerFactory.getLogger(FrServerUtil.class);

    private static String failMsg = "{\"ocrstatus\" : 0}";

    public static void writeOcrFail(String savePath,String objectId){
        try {
            String saveFileName = savePath +"/"+objectId+".json";
            FileUtils.writeStringToFile(new File(saveFileName), failMsg, "gbk");
        }catch (Exception e){
            logger.error("writeOcrFail exception ["+ e.getMessage()+"]");
        }
    }

    /**
     * 目录压缩
     *
     * @param sourceDir
     * @param zipFilePath
     * @return File
     * */
    public static File doZip(String sourceDir, String zipFilePath) throws IOException {
        OutputStream os = null;
        BufferedOutputStream bos = null;
        ZipOutputStream zos = null;
        File zipFile = new File(zipFilePath);
        try {
            File file = new File(sourceDir);
            // 创建写出流操作
            os = new FileOutputStream(zipFile);
            bos = new BufferedOutputStream(os);
            zos = new ZipOutputStream(bos);

            String basePath = null;
            // 获取目录
            if (file.isDirectory()) {
                basePath = file.getPath();
            } else {
                basePath = file.getParent();
            }

            zipFile(file, basePath, zos);
            zos.closeEntry();
        } finally {
            if(zos!=null){
                try {
                    zos.close();
                } catch (Exception e1) {

                }
            }

            if(os!=null){
                try {
                    os.close();
                } catch (Exception e) {

                }
            }

            if(bos!=null){
                try {
                    bos.close();
                } catch (Exception e) {

                }
            }
        }

        return zipFile;
    }

    /**
     * @param source
     *            源文件
     * @param basePath
     * @param zos
     */
    private static void zipFile(File source, String basePath, ZipOutputStream zos) throws IOException {
        File[] files = null;
        if (source.isDirectory()) {
            files = source.listFiles();
        } else {
            files = new File[1];
            files[0] = source;
        }

        String pathName = null;
        byte[] buf = new byte[1024];
        int length = 0;
        for(File file:files){
            if (file.isDirectory()) {
                pathName = file.getPath().substring(basePath.length() + 1) + "/";
                zos.putNextEntry(new ZipEntry(pathName));
                zipFile(file, basePath, zos);
            } else {
                pathName = file.getPath().substring(basePath.length() + 1);


                InputStream is = null;
                BufferedInputStream bis = null;
                try {
                    is = new FileInputStream(file);
                    bis = new BufferedInputStream(is);
                    zos.putNextEntry(new ZipEntry(pathName));
                    while ((length = bis.read(buf)) > 0) {
                        zos.write(buf, 0, length);
                    }
                } finally {
                    if(is!=null){
                        try {
                            is.close();
                        } catch (Exception e) {

                        }
                    }

                    if(bis!=null){
                        try {
                            bis.close();
                        } catch (Exception e) {

                        }
                    }
                }
            }
        }
    }

}
