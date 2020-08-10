package com.hex.ai.fr.serving.util;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hex.ai.commons.model.OcrTask;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class ChangeJsonUtil {

    public static final Logger logger = LoggerFactory.getLogger(ChangeJsonUtil.class);

//
//    public static void main(String args[]){
//        OcrTask modelConfig = new OcrTask();
//        modelConfig.setBatchPath("D:/");
//        modelConfig.setBatchNo("333");
//        modelConfig.setOcrType("FINANCE_REPORT.1.0010");
//        changeJson(modelConfig);
//    }
    public static JSONObject changeJson(OcrTask modelConfig) {
        File file = new File(modelConfig.getBatchPath()+"/json/");
        File[] fileList = file.listFiles();
        //对文件进行排序，把第一页放前面
        List files = Arrays.asList(fileList);
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.isDirectory() && o2.isFile())
                    return -1;
                if (o1.isFile() && o2.isDirectory())
                    return 1;
                int o1Page = -1;
                int o2Page = -1;
                try {
                    String o1FileName = o1.getName();
                    if(StringUtils.isNotEmpty(o1FileName)){
                        o1FileName = o1FileName.substring(0,o1FileName.indexOf("_"));//第一个是页码
                        if(StringUtils.isNotEmpty(o1FileName)){
                            o1Page  = Integer.parseInt(o1FileName);
                        }
                    }

                    String o2FileName = o2.getName();
                    if(StringUtils.isNotEmpty(o2FileName)){
                        o2FileName = o2FileName.substring(0,o2FileName.indexOf("_"));//第一个是页码
                        if(StringUtils.isNotEmpty(o2FileName)){
                            o2Page  = Integer.parseInt(o2FileName);
                        }
                    }
                }catch (Exception e){
                    logger.error("changeJson compare exception = "+e.getMessage());
                }

                return o1Page - o2Page;
            }
        });

        List<File> wjList = new ArrayList<File>();
        ArrayList<JSONObject> jsonObjectArrayList = new ArrayList<JSONObject>();

        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].isFile()) {//判断是否为文件
                if(fileList[i].getName().endsWith(".json")){
                    String str  = "";
                    try {
                        FileReader fileReader = new FileReader(fileList[i]);

                        Reader reader = new InputStreamReader(new FileInputStream(fileList[i]),"utf-8");
                        int ch = 0;
                        StringBuffer sb = new StringBuffer();
                        while ((ch = reader.read()) != -1) {
                            sb.append((char) ch);
                        }
                        fileReader.close();
                        reader.close();
                        str = sb.toString();
                        JSONObject jsonObject = JSONObject.parseObject(str);

                        if (i == 0){
                            jsonObject.put("isFirst","1");
                        }else {
                            jsonObject.put("isFirst","0");
                        }
                        jsonObjectArrayList.add(jsonObject);
                    } catch (IOException e) {
                        e.printStackTrace();

                    }
                }
                wjList.add(fileList[i]);
            }
        }


        JSONArray pages = new JSONArray();

        int pageNumber = 1;
        JSONObject newJson = new JSONObject();

        //转换成需要的json格式并合并
        for(JSONObject jsonObject:jsonObjectArrayList){

            JSONArray rowsJson = new JSONArray();
            JSONArray rowsJsonForcolumn = new JSONArray();
            String fileName = jsonObject.getString("fileName");

            JSONArray jsonTables = jsonObject.getJSONArray("tables");
            if(jsonTables == null) {
                logger.error("changeJson jsonTables is null");
                continue;
            }

            JSONObject tabledescJson = jsonTables.getJSONObject(0);//只取第一个表
            JSONObject tableJson = tabledescJson.getJSONObject("table");

            JSONArray rows = tableJson.getJSONArray("rows");
            int x =1;
            for(int n=0;n<rows.size();n++){
                JSONArray cells = new JSONArray();
                JSONObject rowJson = rows.getJSONObject(n);
                JSONArray cols = rowJson.getJSONArray("cols");
                for(int z=0;z<cols.size();z++){
                    JSONObject col  = new JSONObject();
                    String scol = cols.getJSONObject(z).get("col").toString();
                    if(scol.indexOf(":") < 0) {
                        col.put("col", Integer.parseInt(scol));
                    }
                    else
                    {
                        String colArays[]= scol.split(":");
                        col.put("col",Integer.parseInt(colArays[0]));
                    }
                    col.put("rect",cols.getJSONObject(z).get("rect"));
                    col.put("same","");
                    col.put("text","");
                    col.put("textRect","");
                    col.put("visible",true);
                    col.put("width","");
                    if (n==0&&jsonObject.get("isFirst").equals("1")&&x==1){
                        rowsJsonForcolumn.add(col);
                    }else {
                        cells.add(col);
                    }
                }
                JSONObject rowOfNew  = new JSONObject();

                if (jsonObject.get("isFirst").equals("1")&&x==1){


                }else {
                    rowOfNew.put("PageNo",pageNumber);
                    rowOfNew.put("cells",cells);
                    if (jsonObject.get("isFirst").equals("1")){
                        rowOfNew.put("row",x-1);
                    }else {
                        rowOfNew.put("row",x);
                    }
                    rowsJson.add(rowOfNew);
                }
                x++;
            }
            JSONObject tableNew  = new JSONObject();
            if (jsonObject.get("isFirst").equals("1")){
                tableNew.put("columns",rowsJsonForcolumn);
            }
            tableNew.put("rows",rowsJson);
            JSONObject pageJson = new JSONObject();
            JSONObject head = new JSONObject();
            head.put("company","");
            head.put("cuunit","");
            head.put("fsDate","");
            head.put("title","");
            int extPos = fileName.lastIndexOf(".");
            if(extPos != -1){
                fileName = fileName.substring(0,extPos)+".jpg";
            }
            pageJson.put("fileName",fileName);
            pageJson.put("head",head);
            pageJson.put("page",pageNumber);
            pageNumber++;
            pageJson.put("table",tableNew);
            pages.add(pageJson);
        }
        newJson.put("ocrstatus",1);
        if(pages != null &&pages.size() > 0)
            newJson.put("pageCount",pages.size());
        else {
            //newJson.put("pageCount", 0); //无表格时直接返回null省得后面还去识别
            logger.error("changeJson page size = 0");
            return null;
        }
        newJson.put("pages",pages);
        newJson.put("processTime",1);
        String ocrType = modelConfig.getOcrType();
        String templateCode  = "";
        if(StringUtils.isNotEmpty(ocrType)){
            String[] ocrTypeArray =  ocrType.split("\\.");
            if(ocrTypeArray != null &&ocrTypeArray.length == 3){
                templateCode = ocrTypeArray[2];
            }
        }
        newJson.put("templateCode",templateCode);
        return newJson;
    }

}
