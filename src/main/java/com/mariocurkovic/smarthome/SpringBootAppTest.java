package com.mariocurkovic.smarthome;

import com.mariocurkovic.smarthome.util.PropertiesUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBootAppTest {

	public static void main(String[] args) {
		PropertiesUtil.init();
		SpringApplication.run(SpringBootAppTest.class, args);
	}

}