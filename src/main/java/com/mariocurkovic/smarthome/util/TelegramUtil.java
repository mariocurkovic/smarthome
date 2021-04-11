package com.mariocurkovic.smarthome.util;

import com.mariocurkovic.smarthome.model.MeteoInfo;
import com.mariocurkovic.smarthome.model.telegramapi.TelegramChat;
import com.mariocurkovic.smarthome.model.telegramapi.TelegramMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TelegramUtil {

	private static final Logger logger = LoggerFactory.getLogger(TelegramUtil.class);

	private static boolean firstIteration = true;

	// latest message read in previous iteration
	private static TelegramMessage latestReadMessage;

	// list of received messages (total)
	private static List<TelegramMessage> receivedMessages;

	// list of latest messages for each chat
	private static Map<String, TelegramMessage> latestMessagesByChat = new HashMap<>();

	public static void readMessages() {

		receivedMessages = TelegramApi.getMessages();

		// Prevent further logic in case telegram api is unreachable
		if (receivedMessages == null) {
			return;
		}

		// Handle old messages after app initialization
		if (firstIteration) {
			if (!receivedMessages.isEmpty()) {
				latestReadMessage = receivedMessages.get(receivedMessages.size() - 1);
			}
			firstIteration = false;
			return;
		}

		// Stand by if no new messages received
		if (receivedMessages.isEmpty()) {
			return;
		}

		// Filter only new messages from receivedMessages
		List<TelegramMessage> newMessages = filterNewMessages();

		if (!newMessages.isEmpty()) {
			// handle registration messages
			List<TelegramMessage> registrationMessages = newMessages.stream().filter(TelegramCommand::isRegistrationCommand).collect(Collectors.toList());
			for (TelegramMessage registrationMessage : registrationMessages) {
				handleRegistrationMessage(registrationMessage);
				setLatestMessageByChat(registrationMessage);
				logReceivedMessage(registrationMessage);
			}
			// handle regular messages from registered chats
			List<TelegramMessage> regularMessages = newMessages.stream().filter(TelegramUtil::isMessageRegularCommandFromValidChat).collect(Collectors.toList());
			for (TelegramMessage regularMessage : regularMessages) {
				handleRegularMessage(regularMessage);
				setLatestMessageByChat(regularMessage);
				logReceivedMessage(regularMessage);
			}
			latestReadMessage = receivedMessages.get(receivedMessages.size() - 1);
		}

	}

	private static List<TelegramMessage> filterNewMessages() {
		List<TelegramMessage> newMessages = new ArrayList<>();
		int latestReadMessageTimestamp = latestReadMessage != null ? latestReadMessage.getDate() : -1;
		if (!receivedMessages.isEmpty()) {
			// filter only new messages
			List<TelegramMessage> tmpNewMessages = receivedMessages.stream().filter(telegramMessage -> telegramMessage.getDate() > latestReadMessageTimestamp).collect(Collectors.toList());
			// get unique chat IDs from new messages
			List<Integer> chats = tmpNewMessages.stream().map(telegramMessage -> telegramMessage.getChat().getId()).collect(Collectors.toList()).stream().distinct().collect(Collectors.toList());
			// filter only latest messages by chat
			for (Integer chat : chats) {
				for (int i = tmpNewMessages.size() - 1; i >= 0; i--) {
					if (tmpNewMessages.get(i).getChat().getId().equals(chat)) {
						newMessages.add(tmpNewMessages.get(i));
						break;
					}
				}
			}
		}
		return newMessages;
	}

	private static void handleRegistrationMessage(TelegramMessage message) {
		// handle admin registration token message
		if (TelegramCommand.REGISTRATION_ADMIN.equals(message.getText())) {
			PropertiesUtil.addChat(true, String.valueOf(message.getChat().getId()));
			PropertiesUtil.updateStartupProperties();
			TelegramApi.sendMessage(getStartMessage(message.getChat()), message.getChat());
		}
		// handle user registration token message
		if (TelegramCommand.REGISTRATION_USER.equals(message.getText())) {
			PropertiesUtil.addChat(false, String.valueOf(message.getChat().getId()));
			PropertiesUtil.updateStartupProperties();
			TelegramApi.sendMessage(getStartMessage(message.getChat()), message.getChat());
		}
	}

	private static void handleRegularMessage(TelegramMessage message) {
		if (TelegramCommand.isFirstLevelCommand(message)) {
			handleFirstLevelCommand(message);
		} else if (TelegramCommand.isSecondLevelCommand(getLatestMessageByChat(message.getChat()), message)) {
			handleSecondLevelCommand(getLatestMessageByChat(message.getChat()), message);
		}
	}

	private static void handleFirstLevelCommand(TelegramMessage message) {
		// handle status message
		if (TelegramCommand.STATUS.equals(message.getText())) {
			TelegramApi.sendMessage("Grijanje je " + (GpioUtil.isOn(PropertiesUtil.getRelayPosition()) ? "uključeno" : "isključeno") + ".", message.getChat());
		}
		// handle turn on message
		if (TelegramCommand.TURN_ON.equals(message.getText())) {
			boolean isTurnedOn = GpioUtil.turnOn(PropertiesUtil.getRelayPosition());
			TelegramApi.sendMessage("Grijanje je " + (isTurnedOn ? "uključeno" : "isključeno") + ".", message.getChat());
		}
		// handle turn off message
		if (TelegramCommand.TURN_OFF.equals(message.getText())) {
			boolean isTurnedOff = GpioUtil.turnOff(PropertiesUtil.getRelayPosition());
			TelegramApi.sendMessage("Grijanje je " + (isTurnedOff ? "isključeno" : "uključeno") + ".", message.getChat());
		}
		// handle timer message
		if (TelegramCommand.TIMER.equals(message.getText())) {
			TelegramApi.sendMessage(getTimerMessage(), message.getChat());
		}
		// handle meteo message
		if (TelegramCommand.METEO.equals(message.getText())) {
			TelegramApi.sendMessage(getMeteoInfoMessage(PropertiesUtil.getMeteoLocation()), message.getChat());
		}
		// handle help message
		if (TelegramCommand.HELP.equals(message.getText())) {
			TelegramApi.sendMessage(getHelpMessage(message.getChat()), message.getChat());
		}
	}

	private static void handleSecondLevelCommand(TelegramMessage firstLevelCommand, TelegramMessage secondLevelCommand) {
		// handle timer message
		if (TelegramCommand.TIMER.equals(firstLevelCommand.getText())) {
			// command timeout
			if (isMessageOlderThan(firstLevelCommand, 60)) {
				TelegramApi.sendMessage("Prošlo je previše vremena od zadnje komande. Pokušaj ponovno.", secondLevelCommand.getChat());
			}
			// turn off timer
			else if (TelegramCommand.TIMER_OFF.equals(secondLevelCommand.getText())) {
				PropertiesUtil.turnOffTimer();
				PropertiesUtil.updateStartupProperties();
				TelegramApi.sendMessage("Automatsko uključivanje grijanja je poništeno.", secondLevelCommand.getChat());
			}
			// update timer
			else if (secondLevelCommand.getText().length() == 5 && PropertiesUtil.setTimer(secondLevelCommand.getText())) {
				PropertiesUtil.updateStartupProperties();
				TelegramApi.sendMessage("Postavljeno je automatsko uključivanje grijanja u " + PropertiesUtil.getTimer(), secondLevelCommand.getChat());
			}
			// error message
			else {
				TelegramApi.sendMessage("Nije uspjelo postavljanje timera automatskog uključivanja grijanja. Očekivani format vremena: 'HH:MM' (npr. 08:00)" + PropertiesUtil.getTimer(),
										secondLevelCommand.getChat());
			}
		}
	}

	private static boolean isMessageOlderThan(TelegramMessage message, int numberOfSeconds) {
		if (message == null) {
			return true;
		}
		long diff = (System.currentTimeMillis() / 1000L) - message.getDate();
		return diff > numberOfSeconds;
	}

	private static String getMeteoInfoMessage(String location) {
		MeteoInfo weather = WebParser.getMeteoInfo(location);
		if (weather.getStation() != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("Meteo podatci za ").append(weather.getStation());
			sb.append(" (").append(weather.getLastUpdatedTime()).append("):");
			sb.append("  Temperatura ").append(weather.getTemperature()).append("°");
			sb.append(", Tlak ").append(weather.getPressure()).append("hPa");
			sb.append(", Vlažnost: ").append(weather.getHumidity()).append("%");
			sb.append(".");
			return sb.toString();
		}
		return "Nije moguće dohvatiti meteo podatke.";
	}

	private static String getTimerMessage() {
		if (PropertiesUtil.getTimer() != null) {
			return "Automatsko uključivanje je postavljeno u " + PropertiesUtil.getTimer() + ". Za promjenu, pošalji novo vrijeme u formatu 'HH:MM' (npr. 08:00) ili '/off' za isključivanje timera.";
		}
		return "Automatsko uključivanje još nije postavljeno. Za promjenu, pošalji novo vrijeme automatskog uključivanja u formatu 'HH:MM' (npr. 08:00).";
	}

	private static String getStartMessage(TelegramChat chat) {
		String prepend = "Dobrodošli u razgovor za kontrolu grijanja:\n\n";
		return prepend + getHelpMessage(chat);
	}

	private static String getHelpMessage(TelegramChat chat) {
		StringBuilder sb = new StringBuilder();
		sb.append("Dostupne su sljedeće komande:\n\n");
		// heating control commands
		sb.append("<b>Kontrola grijanja</b>:\n");
		sb.append("/status - vraća status grijanja (uključeno/isključeno)\n");
		sb.append("/ukljuci - uključuje grijanje\n");
		sb.append("/iskljuci - isključuje grijanje\n");
		sb.append("/timer - omogućuje postavljanje automatskog uključivanja grijanja\n\n");
		// other commands
		sb.append("<b>Ostalo</b>:\n");
		sb.append("/meteo - vraća meteo podatke za lokaciju\n");
		sb.append("/pomoc - prikazuje dostupne komande");
		// admin commands
		if (isAdminChat(chat)) {
			// TODO add admin commands
		}
		return sb.toString();
	}

	private static boolean isAdminChat(TelegramChat chat) {
		List<String> adminChats = PropertiesUtil.getAdminChats();
		return adminChats != null && chat != null && adminChats.contains(String.valueOf(chat.getId()));
	}

	private static void setLatestMessageByChat(TelegramMessage message) {
		latestMessagesByChat.put(String.valueOf(message.getChat().getId()), message);
	}

	private static TelegramMessage getLatestMessageByChat(TelegramChat chat) {
		return latestMessagesByChat.get(String.valueOf(chat.getId()));
	}

	private static void logReceivedMessage(TelegramMessage message) {
		StringBuilder sb = new StringBuilder();
		sb.append(" from ").append(message.getFrom().getFirstName() != null ? message.getFrom().getFirstName() : "").append(" ");
		sb.append((message.getFrom().getLastName() != null ? message.getFrom().getLastName() : ""));
		logger.info("Received message" + (!"from".equals(sb.toString().trim()) ? sb.toString() : "") + ": " + message.getText());
	}

	private static boolean isMessageRegularCommandFromValidChat(TelegramMessage telegramMessage) {
		return (TelegramCommand.isFirstLevelCommand(telegramMessage) || TelegramCommand.isSecondLevelCommand(getLatestMessageByChat(telegramMessage.getChat()),
																											 telegramMessage)) && PropertiesUtil.getAllChatList()
																																				.contains(String.valueOf(telegramMessage.getChat()
																																														.getId()));
	}

}













