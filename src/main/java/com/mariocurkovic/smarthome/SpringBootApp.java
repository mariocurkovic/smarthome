package com.mariocurkovic.smarthome;

import com.mariocurkovic.smarthome.util.GpioUtil;
import com.mariocurkovic.smarthome.util.PropertiesUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class SpringBootApp {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootApp.class, args);
		PropertiesUtil.init();
		GpioUtil.init();
	}

}