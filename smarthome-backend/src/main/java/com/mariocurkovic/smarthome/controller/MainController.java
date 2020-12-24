package com.mariocurkovic.smarthome.controller;

import com.mariocurkovic.smarthome.model.Weather;
import com.mariocurkovic.smarthome.util.WebParser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

	@GetMapping("/")
	public String hello() {
		return "Greetings from Spring Boot!";
	}

	@GetMapping("/api/weather/{meteoStation}")
	public Weather getWeatherInfo(@PathVariable String meteoStation) {
		return WebParser.getLocalWeather(meteoStation);
	}

	@GetMapping("/api/heating/{control}")
	public String controlHeating(@PathVariable String control) {
		// VALIDATION
		if (control == null) {
			return "Error calling api. Missing 'control' parameter";
		}
		if (!"on".equalsIgnoreCase(control) && !"off".equalsIgnoreCase(control) && !"status".equalsIgnoreCase(control)) {
			return "Error calling api. Wrong 'control' parameter value. Required 'on', 'off' or 'status'.";
		}
		// EXECUTION LOGIC
		if ("status".equalsIgnoreCase(control)) {
			return "Heating status: ON";
		}
		if ("on".equalsIgnoreCase(control)) {
			return "Heating turned ON";
		}
		if ("off".equalsIgnoreCase(control)) {
			return "Heating turned OFF";
		}
		return "Unable to process request.";
	}

}