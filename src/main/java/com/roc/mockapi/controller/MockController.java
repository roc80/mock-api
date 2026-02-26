package com.roc.mockapi.controller;

import com.roc.apiclientsdk.module.response.ApiResponse;
import com.roc.apiclientsdk.server.ApiServer;
import com.roc.mockapi.ApiSignConstant;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.roc.mockapi.module.User;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author lipeng
 * @since 2026/2/25 10:56
 */
@RestController
@RequestMapping("/name")
@AllArgsConstructor
public class MockController {

    private final ApiServer apiServer;

    @PostMapping("")
    public String name(@RequestBody User user, HttpServletRequest request) {
        String nonce = request.getHeader("nonce");
        String timestamp = request.getHeader("timestamp");
        String bodyJson = request.getHeader("bodyJson");
        String sign = request.getHeader("sign");
        ApiResponse response = apiServer.verifySignature(sign, nonce, timestamp, bodyJson);
        if (response.getCode() != 0) {
            return response.getMessage();
        }
        /*防重放 begin*/
        if (timestamp == null || timestamp.isEmpty()) {
            return "Header: timestamp is null or empty";
        } else if (System.currentTimeMillis() - Long.parseLong(timestamp) > ApiSignConstant.REQUEST_VALID_MINUTES * 60 * 1000) {
            return "request was expired, more than " + ApiSignConstant.REQUEST_VALID_MINUTES + " minutes";
        } else {
            // todo nonce pool 实现
        }
        /*防重放 end*/
        return user.getUsername();
    }

}
