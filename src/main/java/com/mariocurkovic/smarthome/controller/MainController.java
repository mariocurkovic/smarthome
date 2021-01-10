package com.mariocurkovic.smarthome.controller;

import com.mariocurkovic.smarthome.model.MeteoInfo;
import com.mariocurkovic.smarthome.util.GpioUtil;
import com.mariocurkovic.smarthome.util.LogUtil;
import com.mariocurkovic.smarthome.util.PropertiesUtil;
import com.mariocurkovic.smarthome.util.WebParser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

	@GetMapping("/api/weather/{meteoStation}")
	public MeteoInfo getWeatherInfo(@PathVariable String meteoStation) {
		return WebParser.getMeteoInfo(meteoStation);
	}

	@GetMapping("/api/timer/{command}")
	public String manageTimer(@PathVariable String command) {
		if ("status".equals(command)) {
			return PropertiesUtil.getTimer() != null ? PropertiesUtil.getTimer() : "OFF";
		} else if ("off".equals(command)) {
			PropertiesUtil.turnOffTimer();
			PropertiesUtil.updateStartupProperties();
			return "OK";
		} else if (PropertiesUtil.setTimer(command)) {
			PropertiesUtil.updateStartupProperties();
			return "OK";
		}
		return "NOK";
	}

	@GetMapping("/api/heating/{control}/{position}")
	public String controlHeating(@PathVariable String control, @PathVariable String position) {
		// VALIDATION
		if (control == null) {
			return "Error calling api. Missing 'control' parameter";
		}
		if (!"on".equalsIgnoreCase(control) && !"off".equalsIgnoreCase(control) && !"status".equalsIgnoreCase(control)) {
			return "Error calling api. Wrong 'control' parameter value. Required 'on', 'off' or 'status'.";
		}
		if (!validatePositionParameter(position)) {
			return "Error calling api. Wrong 'position' parameter value. Required double digit number between '00' and '31'.";
		}
		// EXECUTION LOGIC
		if ("status".equalsIgnoreCase(control)) {
			return GpioUtil.isOn(position) ? "ON" : "OFF";
		}
		if ("on".equalsIgnoreCase(control)) {
			return GpioUtil.turnOn(position) ? "OK" : "NOK";
		}
		if ("off".equalsIgnoreCase(control)) {
			return GpioUtil.turnOff(position) ? "OK" : "NOK";
		}
		return "Unable to process request.";
	}

	@GetMapping(value = {"/api/logs", "/api/logs/{filename}"})
	public String readLogs(@PathVariable(required = false) String filename) {
		if (filename == null) {
			return LogUtil.getListOfLogFiles();
		}
		return LogUtil.getLogFileContent(filename);
	}

	private boolean validatePositionParameter(String position) {
		if (position != null) {
			if (position.length() != 2) {
				return false;
			}
			try {
				int pos = Integer.parseInt(position);
				return (pos >= 0 && pos < 32);
			} catch (NumberFormatException e) {
				return false;
			}
		}
		return false;
	}

}