package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RequestMapping;

@SpringBootApplication
@ComponentScan("com.*")
public class H5sseApplication {

	public static void main(String[] args) {
		SpringApplication.run(H5sseApplication.class, args);
	}


}
