package com.mariocurkovic.smarthome.util;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Properties;

public class PropertiesUtil {

	private static final Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);

	private static final String startupPropertiesFilename = "startup.properties";

	@Getter
	public static String timer;

	@Getter
	private static String initialStatus = "OFF";

	public static void loadStartupProperties() {
		try (InputStream input = new FileInputStream("startup.properties")) {
			Properties prop = new Properties();
			prop.load(input);
			// set startup properties if they exist
			if (prop.containsKey("app.initialStatus")) {
				setInitialStatus(prop.getProperty("app.initialStatus"));
			}
			if (prop.containsKey("app.timer")) {
				setTimer(prop.getProperty("app.timer"));
			}
		} catch (IOException ex) {
			// do nothing
		}
	}

	public static void updateStartupProperties() {
		try (OutputStream output = new FileOutputStream("startup.properties")) {
			Properties prop = new Properties();
			if (initialStatus != null) {
				prop.setProperty("app.initialStatus", initialStatus);
			}
			if (timer != null) {
				prop.setProperty("app.timer", timer);
			}
			prop.store(output, null);
		} catch (IOException ex) {
			logger.error("Unable to save startup.properties file. " + ex.getMessage());
		}
	}

	public static boolean setInitialStatus(String initialStatusProperty) {
		if ("ON".equals(initialStatusProperty) || "OFF".equals(initialStatusProperty)) {
			initialStatus = initialStatusProperty;
			logger.info("Initial status set to: " + initialStatus + "");
			return true;
		}
		logger.error("Invalid initial status format: " + initialStatusProperty + ". Required 'ON' or 'OFF'");
		return false;
	}

	public static boolean setTimer(String timerProperty) {
		if (timerProperty != null && timerProperty.matches("^\\d{2}:\\d{2}$")) {
			try {
				int hours = Integer.parseInt(timerProperty.substring(0, 2));
				int minutes = Integer.parseInt(timerProperty.substring(3));
				if (hours >= 0 && hours < 24 && minutes >= 0 && minutes < 60) {
					timer = timerProperty;
					logger.info("Timer set at: " + timer);
					return true;
				}
			} catch (NumberFormatException e) {
				// do nothing
			}
		}
		logger.error("Invalid timer format: " + timerProperty + ". Required time in format 'HH:MM'");
		return false;
	}

	public static boolean isTimeForTimer() {
		if (timer != null) {
			try {
				Calendar now = Calendar.getInstance();
				int currentHours = now.get(Calendar.HOUR);
				int currentMinutes = now.get(Calendar.MINUTE);
				int timerHours = Integer.parseInt(timer.substring(0, 2));
				int timerMinutes = Integer.parseInt(timer.substring(3));
				return currentHours == timerHours && currentMinutes == timerMinutes;
			} catch (NumberFormatException e) {
				// do nothing
			}
		} return false;
	}
}
