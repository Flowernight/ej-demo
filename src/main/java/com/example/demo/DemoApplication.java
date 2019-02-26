package com.example.demo;

import com.example.demo.controller.HelloWorldController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication
/*@EnableAutoConfiguration*/
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(HelloWorldController.class, args);
	}
}
