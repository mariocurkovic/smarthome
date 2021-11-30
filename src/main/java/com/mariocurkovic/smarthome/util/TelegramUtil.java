package com.mariocurkovic.smarthome.util;

import com.mariocurkovic.smarthome.model.telegramapi.TelegramChat;
import com.mariocurkovic.smarthome.model.telegramapi.TelegramMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TelegramUtil {

	private static final Logger logger = LoggerFactory.getLogger(TelegramUtil.class);

	// latest message read in previous iteration
	private static TelegramMessage latestReadMessage;

	// list of received messages (total)
	private static List<TelegramMessage> receivedMessages;

	// is first program iteration after boot
	public static boolean firstIteration = true;

	public static void readMessages() {

		receivedMessages = TelegramApi.getMessages();
		TelegramMessage newMessage = getNewMessage();

		if (firstIteration) {
			latestReadMessage = newMessage;
			firstIteration = false;
			return;
		}

		// Return if no new messages available
		if (newMessage == null) {
			return;
		}

		handleMessage(newMessage);
		logReceivedMessage(newMessage);
		latestReadMessage = newMessage;

	}

	private static TelegramMessage getNewMessage() {
		if (receivedMessages != null && !receivedMessages.isEmpty() && (latestReadMessage == null || (!receivedMessages.get(receivedMessages.size() - 1)
																													   .getDate()
																													   .equals(latestReadMessage.getDate())))) {
			return receivedMessages.get(receivedMessages.size() - 1);
		}
		return null;
	}

	private static void handleMessage(TelegramMessage message) {
		// handle status message
		if (TelegramCommand.STATUS.equals(message.getText().toLowerCase())) {
			TelegramApi.sendMessage("Grijanje je " + (GpioUtil.isOn(PropertiesUtil.getRelayPosition()) ? "uključeno" : "isključeno") + ".", message.getChat());
		}
		// handle turn on message
		else if (TelegramCommand.TURN_ON.equals(message.getText().toLowerCase())) {
			boolean isTurnedOn = GpioUtil.turnOn(PropertiesUtil.getRelayPosition());
			TelegramApi.sendMessage("Grijanje je " + (isTurnedOn ? "uključeno" : "isključeno") + ".", message.getChat());
		}
		// handle turn off message
		else if (TelegramCommand.TURN_OFF.equals(message.getText().toLowerCase())) {
			boolean isTurnedOff = GpioUtil.turnOff(PropertiesUtil.getRelayPosition());
			TelegramApi.sendMessage("Grijanje je " + (isTurnedOff ? "isključeno" : "uključeno") + ".", message.getChat());
		}
		// handle timer status message
		else if (TelegramCommand.TIMER_STATUS.equals(message.getText().toLowerCase())) {
			TelegramApi.sendMessage(getTimerMessage(), message.getChat());
		}
		// handle timer off message
		else if (TelegramCommand.TIMER_OFF.equals(message.getText().toLowerCase())) {
			PropertiesUtil.turnOffTimer();
			PropertiesUtil.updateStartupProperties();
			TelegramApi.sendMessage("Automatsko uključivanje grijanja je poništeno.", message.getChat());
		}
		// handle timer message
		else if (message.getText().toLowerCase().startsWith(TelegramCommand.TIMER) && PropertiesUtil.setTimer(message.getText())) {
			TelegramApi.sendMessage(getTimerMessage(), message.getChat());
			PropertiesUtil.updateStartupProperties();
		}
		// handle help message
		else if (TelegramCommand.HELP.equals(message.getText().toLowerCase()) || ("/" + TelegramCommand.HELP).equals(message.getText().toLowerCase())) {
			TelegramApi.sendMessage(getHelpMessage(message.getChat()), message.getChat());
		} else {
			TelegramApi.sendMessage("Nevažeća komanda. Za sve dostupne komande koristite /pomoc.", message.getChat());
		}
	}

	private static String getTimerMessage() {
		if (PropertiesUtil.getTimer() != null) {
			return "Automatsko uključivanje je postavljeno u " + PropertiesUtil.getTimer() + ". \n\nZa promjenu, pošalji novo vrijeme u formatu <b>'timer HH:MM'</b> (npr. <b>'timer 08:00'</b>) ili <b>'timer off'</b> za isključivanje timera.";
		}
		return "Automatsko uključivanje još nije postavljeno. \n\nZa promjenu, pošalji novo vrijeme automatskog uključivanja u formatu <b>'timer HH:MM'</b> (npr. <b>'timer 08:00'</b>).";
	}

	private static String getHelpMessage(TelegramChat chat) {
		StringBuilder sb = new StringBuilder();
		sb.append("Dostupne su sljedeće komande:\n\n");
		// heating control commands
		sb.append("<b>status</b> - vraća status grijanja (uključeno/isključeno)\n");
		sb.append("<b>ukljuci</b> - uključuje grijanje\n");
		sb.append("<b>iskljuci</b> - isključuje grijanje\n");
		sb.append("<b>timer</b> - prikazuje postavke automatskog uključivanja grijanja\n");
		sb.append("<b>timer off</b> - isključuje automatsko uključivanje grijanja\n");
		sb.append("<b>timer HH:MM</b> - postavlja automatsko uključivanje grijanja (npr. 'timer 08:00')\n\n");
		// other commands
		sb.append("/pomoc - prikazuje dostupne komande");

		return sb.toString();
	}

	private static void logReceivedMessage(TelegramMessage message) {
		StringBuilder sb = new StringBuilder();
		sb.append(" from ").append(message.getFrom().getFirstName() != null ? message.getFrom().getFirstName() : "").append(" ");
		sb.append((message.getFrom().getLastName() != null ? message.getFrom().getLastName() : ""));
		logger.info("Received message" + (!"from".equals(sb.toString().trim()) ? sb.toString() : "") + ": " + message.getText());
	}

}













