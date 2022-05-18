package com.zdk.seckilldemo.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.zdk.seckilldemo.pojo.User;
import com.zdk.seckilldemo.service.UserService;
import com.zdk.seckilldemo.vo.ApiResp;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author zdk
 * @since 2022-05-15
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/info")
    public ApiResp info(User user){
        return ApiResp.success(user);
    }

    @GetMapping("/createUser")
    @ApiOperation("压测创建配置文件")
    public void createUser() throws IOException {
        List<User> list = new ArrayList<>();
//        //生成用户
//        for (int i = 500; i < 5000; i++) {
//            User user = new User();
//            user.setId(1233L + i);
//            user.setNickname("user" + i);
//            user.setSalt("zdk");
//            user.setPassword("283d8bcd6d88946bbb597edf952fc2f8");
//            list.add(user);
//        }
//        userService.saveBatch(list);
//        System.out.println("create user");

//        //读取用户
        list = userService.list();

        //登录，生成UserTicket
        String urlString = "http://localhost:8080/login/doLogin";
        File file = new File("config.txt");
        System.out.println(file.exists());
        if (file.exists()) {
            file.delete();
        }
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        randomAccessFile.seek(0);
        for (User user : list) {
            URL url = new URL(urlString);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            String params = "mobile=" + user.getId() + "&password=d3b1294a61a07da9b49b6e22b2cbd7f9";
            outputStream.write(params.getBytes());
            outputStream.flush();
            InputStream inputStream = httpURLConnection.getInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buff)) >= 0) {
                byteArrayOutputStream.write(buff, 0, len);
            }
            inputStream.close();
            byteArrayOutputStream.close();
            String respone = new String(byteArrayOutputStream.toByteArray());
            System.out.println("respone = " + respone);
            ObjectMapper mapper = new ObjectMapper();
            ApiResp apiResp = mapper.readValue(respone, ApiResp.class);
            String userTicket = (String) apiResp.getObject();
            System.out.println("create userTicket:" + user.getId());
            String row = user.getId() + "," + userTicket;
            randomAccessFile.seek(randomAccessFile.length());
            randomAccessFile.write(row.getBytes());
            randomAccessFile.write("\r\n".getBytes());
            System.out.println("write to file :" + user.getId());
        }
        randomAccessFile.close();
        System.out.println();
    }

}

