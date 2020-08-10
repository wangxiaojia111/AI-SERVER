package com.hex.ai.fr.serving;

import com.alibaba.fastjson.JSON;
import com.squareup.okhttp.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

//@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void test() throws Exception {


//        ResponseEntity<String> response = restTemplate.postForEntity("/hex/ocrapi/fr", getParams(), String.class);
//        System.out.println(response.getStatusCode());
    }

    private String getParams(){
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("Priority",2);
        map.put("BatchPath","D:/exdoc/waitprocess\\\\090122ca51");
        map.put("BatchNo","090122ca51");
        map.put("City","FINANCE_REPORT");
        map.put("_id","1b7a65db-f4d5-4d78-91c7-511a6fc55c3b");
        map.put("ExportPath","D:/exdoc/export\\\\090122ca51");
        map.put("CreateTime","ISODate(\"2019-04-22T03:24:59.043Z\") ");
                String strMsg = JSON.toJSONString(map);
        return strMsg;
    }

    @Test
    public void test1(){
//        OkHttpClient client = new OkHttpClient();
//
//        MediaType mediaType = MediaType.parse("application/json");
//        RequestBody body = RequestBody.create(mediaType, "{\n    \"batchNo\": \"111\",\n    \"objectId\": \"222\"\n}");
//        Request request = new Request.Builder()
//                .url("http://127.0.0.1:59020/hex/ocrapi/frocr")
//                .post(body)
//                .addHeader("Content-Type", "application/json")
//                .build();
//
//        try {
//            Response response = client.newCall(request).execute();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
