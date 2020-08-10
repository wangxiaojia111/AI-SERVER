package com.hex.ai.fr.serving.util;

import com.hex.ai.fr.serving.controller.FrController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Created by wzj on 2016/9/9.
 */
public class FileZipUtils {
    public static final Logger logger = LoggerFactory.getLogger(FileZipUtils.class);

    /**
     *
     * 解压到指定目录
     * @param zipPath  要解压的zip文件路径
     * @param descDir  解压到目标目录
     * @throws IOException
     */
    public static void unZipFiles(String zipPath,String descDir)throws IOException
    {
        unZipFiles(new File(zipPath), descDir);
    }
    /**
     * 解压文件到指定目录
     */
    @SuppressWarnings("rawtypes")
    public static void unZipFiles(File zipFile,String descDir)throws IOException
    {
        File pathFile = new File(descDir);
        logger.error("descDir=="+ pathFile.getAbsolutePath());
        if(!pathFile.exists())
        {
            pathFile.mkdirs();
        }
        //解决zip文件中有中文目录或者中文文件
        ZipFile zip = new ZipFile(zipFile, Charset.forName("GBK"));
        for(Enumeration entries = zip.entries(); entries.hasMoreElements();)
        {
            ZipEntry entry = (ZipEntry)entries.nextElement();
            String zipEntryName = entry.getName();
            InputStream in = zip.getInputStream(entry);
            String outPath = (descDir+zipEntryName).replaceAll("\\\\", "/");
            //判断路径是否存在,不存在则创建文件路径
            boolean ismkdir = false;
            if(outPath.lastIndexOf("/") != -1){ //检查此文件是否带有文件夹
                ismkdir = true;
            }
            File file = null;
            if(ismkdir){
                file = new File(outPath.substring(0, outPath.lastIndexOf("/")));
            }else{
                file = new File(outPath);
            }

            if(ismkdir && !file.exists())
            {
                file.mkdirs();
            }
            //判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
            if(new File(outPath).isDirectory())
            {
                continue;
            }
            //输出文件路径信息
            System.out.println(outPath);
            OutputStream out = new FileOutputStream(outPath);
            byte[] buf1 = new byte[1024];
            int len;
            while((len=in.read(buf1))>0)
            {
                out.write(buf1,0,len);
            }
            in.close();
            out.close();
        }
        zip.close();
        logger.error("******************解压完毕********************");
    }

    /**
     * 目录压缩
     *
     * @param sourceDir  需要压缩的目录或文件
     * @param zipFilePath  生成的zip目标文件路径
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

            String basePath = file.getParent();
            // 获取目录
            if (file.isDirectory()) {
                String filePath = file.getPath();
                String pathName = filePath.substring(basePath.length() + 1) + "/";
                zos.putNextEntry(new ZipEntry(pathName));
            }

            zipFile(file, basePath, zos);
            zos.closeEntry();
        } finally {
            if(zos!=null){
                try {
                    zos.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            if(os!=null){
                try {
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if(bos!=null){
                try {
                    bos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return zipFile;
    }

    /**
     * @param source 源文件
     * @param basePath 源文件父目录
     * @param zos
     */
    private static void zipFile(File source, String basePath, ZipOutputStream zos) throws IOException {
        File[] files = null;
        String pathName = null;
        if (source.isDirectory()) {
            files = source.listFiles();
        } else {
            files = new File[1];
            files[0] = source;
        }


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

    public static boolean delFile(File file) {
        if(!file.exists()) {
            return false;
        }
        if(file.isDirectory()) {
            File[] files = file.listFiles();
            for(File f : files) {
                delFile(f);
            }
        }
        return  file.delete();
    }

    public static void main(String[] args) throws IOException {
//        /**
//         * 解压文件
//         */
//        String filePath = "C:\\Users\\Administrator\\Desktop\\影像平台下载文件代码示例.zip";
//        String path = "C:\\Users\\Administrator\\Desktop\\";
//        unZipFiles(filePath, path);

        /**
         * 压缩文件
         */

//        String filePath = "D:\\exdoc\\temp\\0901bb841_export.zip";
//        String path = "D:\\exdoc\\export\\0901bb841\\";
//        doZip(path,filePath);
//        String filePath1 = "D:\\exdoc\\temp\\0901bb841_waitprocess.zip";
//        String path1 = "D:\\exdoc\\waitprocess\\0901bb841\\";
//        doZip(path1,filePath1);

//        dozipBatchNoDir("0901214221");
//        unZipBatchNoDir("0901214221");

//        unZipBatchNoDir("L:\\exdoc\\export\\09014b11","09014b11");
    }

//    public static String unZipBatchNoDir(String exportPath,String batchNo) throws IOException{
//        File exportDir = new File(exportPath);
//        if(!exportDir.exists()){
//            exportDir.mkdir();
//        }
//        String tempDir = "L:\\exdoc" + "\\temp\\" + batchNo ;
//        String waitprocessPath ="L:\\exdoc" + "\\waitprocess\\";
//        FileZipUtils.unZipFiles(tempDir + ".zip",waitprocessPath);
//
//        org.apache.commons.io.FileUtils.deleteDirectory(new File(tempDir));
//        org.apache.commons.io.FileUtils.forceDeleteOnExit(new File(tempDir+".zip"));
//        return tempDir + ".zip";
//    }

    /**
     * base64字符串转化成文件
     * @param imgStr
     * @param saveFileName
     * @return
     */
    public static boolean getFileFromBase64(String imgStr,String saveFileName) {

        if (imgStr == null) //图像数据为空
            return false;
        BASE64Decoder decoder = new BASE64Decoder();
        try
        {
            byte[] b = decoder.decodeBuffer(imgStr);
            for(int i = 0;i < b.length;++i) {
                if(b[i] < 0) {//调整异常数据
                    b[i] += 256;
                }
            }

            OutputStream out = new FileOutputStream(saveFileName);
            out.write(b);out.flush();out.close();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * 文件转base64字符串
     * @param imageFile
     * @return
     * @throws IOException
     */
    public static String getBase64FromFile(File imageFile)
            throws IOException {
        if (imageFile.exists()) {
            StringBuffer base64 = new StringBuffer(4096);
            try (InputStream in = new FileInputStream(imageFile);){
                byte data[] = new byte[(int) imageFile.length()]; // 创建合适文件大小的数组
                in.read(data); // 读取文件中的内容到b[]数组
                base64.append(Base64Util.encode(data));
            }
            return base64.toString();
        } else {
            return null;
        }


    }
}