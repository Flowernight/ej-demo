package com.example.demo.controller;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by xulihua on 2018/1/11.
 */
@RestController
//@EnableAutoConfiguration
public class HelloWorldController {

    @RequestMapping("index")
    public String index(){
        return "index";
    }
}
