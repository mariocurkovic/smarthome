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

	// read from app properties
	@Getter
	private static String startupPropertiesFilename;
	@Getter
	private static String telegramChatId;
	@Getter
	private static String telegramChatName;
	@Getter
	private static String telegramClientName;
	@Getter
	private static String telegramClientToken;
	@Getter
	private static String meteoLocation;
	@Getter
	private static String relayPosition;

	// read from startup properties
	@Getter
	public static String timer;
	@Getter
	private static String initialStatus = "OFF";

	public static void init() {
		loadAppProperties();
		loadStartupProperties();
	}

	/**
	 * loads app properties from resources
	 */
	private static void loadAppProperties() {
		try (InputStream input = PropertiesUtil.class.getResourceAsStream("/app.properties")) {
			Properties prop = new Properties();
			if (input == null) {
				logger.error("Unable to find app.properties");
				return;
			}
			prop.load(input);

			// load app properties
			startupPropertiesFilename = prop.getProperty("app.startup.properties");
			telegramChatId = prop.getProperty("app.telegram.chat.id");
			telegramChatName = prop.getProperty("app.telegram.chat.name");
			telegramClientName = prop.getProperty("app.telegram.client.id");
			telegramClientToken = prop.getProperty("app.telegram.client.token");
			meteoLocation = prop.getProperty("app.meteo.location");
			relayPosition = prop.getProperty("app.relay.position");

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * loads startup (initial) properties from file
	 */
	public static void loadStartupProperties() {
		try (InputStream input = new FileInputStream(startupPropertiesFilename)) {
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

	/**
	 * updates startup (initial) properties file
	 */
	public static void updateStartupProperties() {
		try (OutputStream output = new FileOutputStream(startupPropertiesFilename)) {
			Properties prop = new Properties();
			if (initialStatus != null) {
				prop.setProperty("app.initialStatus", initialStatus);
			}
			if (timer != null) {
				prop.setProperty("app.timer", timer);
			}
			prop.store(output, null);
		} catch (IOException ex) {
			logger.error("Unable to save " + startupPropertiesFilename + " file. " + ex.getMessage());
		}
	}

	/**
	 * sets initial relay status
	 */
	public static void setInitialStatus(String initialStatusProperty) {
		if ("ON".equals(initialStatusProperty) || "OFF".equals(initialStatusProperty)) {
			initialStatus = initialStatusProperty;
			logger.info("Initial status set to: " + initialStatus + "");
		}
		logger.error("Invalid initial status format: " + initialStatusProperty + ". Required 'ON' or 'OFF'");
	}

	/**
	 * sets timer
	 */
	public static boolean setTimer(String timerString) {
		if (isValidTimerString(timerString)) {
			timer = timerString;
			logger.info("Timer set at: " + timerString);
			return true;
		}
		logger.error("Invalid timer format: " + timerString + ". Required time in format 'HH:MM'");
		return false;
	}

	/**
	 * validates provided times string
	 */
	public static boolean isValidTimerString(String timerString) {
		if (timerString != null && timerString.matches("^\\d{2}:\\d{2}$")) {
			try {
				int hours = Integer.parseInt(timerString.substring(0, 2));
				int minutes = Integer.parseInt(timerString.substring(3));
				if (hours >= 0 && hours < 24 && minutes >= 0 && minutes < 60) {
					return true;
				}
			} catch (NumberFormatException e) {
				// do nothing
			}
		}
		return false;
	}

	/**
	 * checks whether current time matches time set in timer
	 */
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
		}
		return false;
	}

}
