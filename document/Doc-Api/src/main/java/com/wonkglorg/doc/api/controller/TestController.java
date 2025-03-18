package com.wonkglorg.doc.api.controller;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("test")
public class TestController {

    @PutMapping("test")
    public void test(@RequestBody TestRequest testRequest) {

    }
}
