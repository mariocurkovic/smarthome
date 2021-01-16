package com.mariocurkovic.smarthome.util;

import com.mariocurkovic.smarthome.model.telegramapi.TelegramMessage;

public class TelegramCommand {

	// heating control commands
	public static String STATUS = "/status";
	public static String TURN_ON = "/ukljuci";
	public static String TURN_OFF = "/iskljuci";
	public static String TIMER = "/timer";
	public static String TIMER_OFF = "/off";

	// other commands
	public static String METEO = "/meteo";
	public static String HELP = "/pomoc";

	// registration commands
	public static String REGISTRATION_ADMIN = PropertiesUtil.getRegisterTokenAdmin();
	public static String REGISTRATION_USER = PropertiesUtil.getRegisterTokenUser();

	public static boolean isRegistrationCommand(TelegramMessage message) {
		return message != null && (REGISTRATION_ADMIN.equals(message.getText()) || REGISTRATION_USER.equals(message.getText()));
	}

	public static boolean isFirstLevelCommand(TelegramMessage message) {
		return (message != null && (STATUS.equals(message.getText()) || TURN_ON.equals(message.getText()) || TURN_OFF.equals(message.getText()) || TIMER.equals(message.getText()) || METEO.equals(
				message.getText()) || HELP.equals(message.getText())));
	}

	public static boolean isSecondLevelCommand(TelegramMessage firstLevelCommand, TelegramMessage secondLevelCommand) {
		if (firstLevelCommand == null || secondLevelCommand == null) {
			return false;
		}
		if (TIMER.equals(firstLevelCommand.getText())) {
			if (TIMER_OFF.equals(secondLevelCommand.getText())) {
				return true;
			}
			if (secondLevelCommand.getText().length() == 5 && PropertiesUtil.isValidTimerString(secondLevelCommand.getText())) {
				return true;
			}
		}
		return false;
	}

}
