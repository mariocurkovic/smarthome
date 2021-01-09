package com.mariocurkovic.smarthome;

import com.mariocurkovic.smarthome.util.GpioUtil;
import com.mariocurkovic.smarthome.util.PropertiesUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBootApp {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootApp.class, args);
		PropertiesUtil.loadStartupProperties();
		GpioUtil.startGpioController();
	}

}