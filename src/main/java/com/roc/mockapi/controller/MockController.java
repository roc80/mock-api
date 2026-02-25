package com.roc.mockapi.controller;

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
public class MockController {

    @PostMapping("")
    public String name(@RequestBody User user, HttpServletRequest request) {

        return user.getUsername();
    }

}
