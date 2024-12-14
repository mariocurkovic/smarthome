package com.mariocurkovic.smarthome;

import com.mariocurkovic.smarthome.util.GpioUtil;
import com.mariocurkovic.smarthome.util.PropertiesUtil;
import com.mariocurkovic.smarthome.util.TuyaConsumerUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBootApp {

	public static void main(String[] args) throws Exception {
		PropertiesUtil.init();
		SpringApplication.run(SpringBootApp.class, args);
		GpioUtil.init();
		TuyaConsumerUtil.init();
	}

}