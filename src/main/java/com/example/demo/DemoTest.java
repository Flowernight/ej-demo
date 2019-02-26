package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by xulihua on 2018/1/17.
 */
//@ComponentScan(basePackages = "com.example.demo.controller")
//@EnableAutoConfiguration.controller
@SpringBootApplication(scanBasePackages = "com.example.demo")
public class DemoTest {

   public static void main(String[] args) {
       SpringApplication.run(DemoTest.class,args);
   }
}
