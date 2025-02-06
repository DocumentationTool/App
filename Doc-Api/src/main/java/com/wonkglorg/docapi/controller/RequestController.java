package com.wonkglorg.docapi.controller;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

@Controller
public class RequestController {

    @RequestMapping(value = "/customers/{cid}/orders/{oid}", method = GET)
    public void init() {
        System.out.println("Initializing Routes");
    }
}
