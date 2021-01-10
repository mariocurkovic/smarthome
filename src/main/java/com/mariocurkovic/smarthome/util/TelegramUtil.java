package com.mariocurkovic.smarthome.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mariocurkovic.smarthome.model.Chat;
import com.mariocurkovic.smarthome.model.Client;
import com.mariocurkovic.smarthome.model.Message;
import com.mariocurkovic.smarthome.model.MeteoInfo;
import com.mariocurkovic.smarthome.model.telegramapi.TelegramMessage;
import com.mariocurkovic.smarthome.model.telegramapi.TelegramReceiveModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class TelegramUtil {

	private static final Logger logger = LoggerFactory.getLogger(TelegramUtil.class);

	private static final Chat chat = new Chat(PropertiesUtil.getTelegramChatId(), PropertiesUtil.getTelegramChatName(), true);
	private static final Client sender = new Client(PropertiesUtil.getTelegramClientName(),
													PropertiesUtil.getTelegramClientToken());

	private static Integer lastUpdateId = -1;
	private static TelegramMessage previousMessage;
	private static boolean isFirstMessage = true;

	public static void readMessages() {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
										 .uri(URI.create("https://api.telegram.org/bot" + PropertiesUtil.getTelegramClientToken() + "/getUpdates"))
										 .build();
		try {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			ObjectMapper objectMapper = new ObjectMapper();
			TelegramReceiveModel receiveModel = objectMapper.readValue(response.body().toString(), TelegramReceiveModel.class);
			TelegramMessage lastReceivedMessage = receiveModel.getResult().get(receiveModel.getResult().size() - 1).getMessage();
			Integer lastReceivedMessageUpdateId = receiveModel.getResult().get(receiveModel.getResult().size() - 1).getUpdateId();

			if (receiveModel.getResult() != null && receiveModel.getResult().size() > 0 && !lastReceivedMessageUpdateId.equals(
					lastUpdateId)) {

				if (isFirstMessage) {
					lastUpdateId = lastReceivedMessageUpdateId;
					isFirstMessage = false;
					return;
				}

				String lastReceivedMessageSender = lastReceivedMessage.getFrom()
																	  .getFirstName() + " " + lastReceivedMessage.getFrom()
																												 .getLastName();
				logger.info("Received new message from " + lastReceivedMessageSender + ": " + lastReceivedMessage.getText());

				lastUpdateId = lastReceivedMessageUpdateId;
				handleMessage(lastReceivedMessage);
				previousMessage = lastReceivedMessage;

			}
		} catch (IOException | InterruptedException e) {
			// do nothing
		} catch (NullPointerException e) {
			logger.error("Unable to parse message.");
		}

	}

	/**
	 * sends message to telegram chat
	 */
	public static boolean sendMessage(Message message) {

		HttpClient client = HttpClient.newBuilder()
									  .connectTimeout(Duration.ofSeconds(5))
									  .version(HttpClient.Version.HTTP_2)
									  .build();

		UriBuilder builder = UriBuilder.fromUri("https://api.telegram.org")
									   .path("/{token}/sendMessage")
									   .queryParam("chat_id", message.getChat().getId())
									   .queryParam("text", message.getText());

		HttpRequest request = HttpRequest.newBuilder()
										 .GET()
										 .uri(builder.build("bot" + message.getSender().getToken()))
										 .timeout(Duration.ofSeconds(5))
										 .build();

		HttpResponse<String> response = null;

		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException | InterruptedException e) {
			System.out.println("Unable to send message: " + message.toString() + ". " + e.getMessage());
			e.printStackTrace();
		}

		if (response.statusCode() != 200) {
			System.out.println("Unable to send message: " + message.toString() + ". Response: " + response.statusCode() + " " + response
					.body());
		}

		return response.statusCode() == 200;

	}

	/**
	 * handles incoming message
	 */
	private static void handleMessage(TelegramMessage receivedMessage) {
		if (isValidLvl1Message(receivedMessage)) {
			handleLevel1Message(receivedMessage);
		} else if (isValidLvl2Message(previousMessage, receivedMessage)) {
			handleLevel2Message(previousMessage, receivedMessage);
		}
	}

	/**
	 * handles incoming level 1 messages
	 */
	private static void handleLevel1Message(TelegramMessage receivedMessage) {

		String receivedMessageText = receivedMessage != null ? receivedMessage.getText() : "";

		switch (receivedMessageText) {
			case "/status":
				sendMessage("Grijanje je " + (GpioUtil.isOn(PropertiesUtil.getRelayPosition())
											  ? "uključeno"
											  : "isključeno") + ".");
				return;
			case "/ukljuci":
				boolean isTurnedOn = GpioUtil.turnOn(PropertiesUtil.getRelayPosition());
				sendMessage("Grijanje je " + (isTurnedOn ? "uključeno" : "isključeno") + ".");
				return;
			case "/iskljuci":
				boolean isTurnedOff = GpioUtil.turnOff(PropertiesUtil.getRelayPosition());
				sendMessage("Grijanje je " + (isTurnedOff ? "isključeno" : "uključeno") + ".");
				return;
			case "/meteo":
				sendMessage(getMeteoInfoMessage(PropertiesUtil.getMeteoLocation()));
				return;
			case "/timer":
				sendMessage(getTimerMessage());
				return;
			case "/pomoc":
				StringBuilder sb = new StringBuilder();
				sb.append("Dostupne komande:\n");
				sb.append("/status - vraća status grijanja (uključeno/isključeno)\n");
				sb.append("/ukljuci - uključuje grijanje\n");
				sb.append("/iskljuci - isključuje grijanje\n");
				sb.append("/meteo - vraća meteo podatke za lokaciju\n");
				sb.append("/timer - omogućuje postavljanje automatskog uključivanja grijanja\n");
				sb.append("/pomoc - prikazuje dostupne komande");
				sendMessage(sb.toString());
				return;
			default:
				// do nothing
		}

	}

	/**
	 * handles incoming level 2 messages
	 */
	private static void handleLevel2Message(TelegramMessage lvl1Message, TelegramMessage lvl2Message) {
		if (!isValidLvl2Message(lvl1Message, lvl2Message)) {
			return;
		}

		String lvl1MessageText = lvl1Message != null ? lvl1Message.getText() : "";
		String lvl2MessageText = lvl2Message != null ? lvl2Message.getText() : "";

		switch (lvl1MessageText) {
			case "/timer":
				if (isMessageOlderThan(lvl1Message, 60)) {
					sendMessage("Prošlo je previše vremena od zadnje komande. Pokušaj ponovno.");
				} else if ("/off".equals(lvl2MessageText)) {
					PropertiesUtil.timer = null;
					PropertiesUtil.updateStartupProperties();
					sendMessage("Automatsko uključivanje grijanja je poništeno.");
				} else if (lvl2MessageText.length() > 1 && PropertiesUtil.setTimer(lvl2MessageText.substring(1))) {
					PropertiesUtil.updateStartupProperties();
					sendMessage("Postavljeno je automatsko uključivanje grijanja u " + PropertiesUtil.getTimer());
				} else {
					sendMessage(
							"Nije uspjelo postavljanje timera automatskog uključivanja grijanja. Očekivani format vremena: '/HH:MM' (npr. /08:00)" + PropertiesUtil
									.getTimer());
				}
				return;
			default:
				// do nothing
		}

	}

	/**
	 * Is received message level 1
	 */
	private static boolean isValidLvl1Message(TelegramMessage message) {
		if (message != null) {
			String messageText = message.getText();
			return ("/timer".equals(messageText) || "/status".equals(messageText) || "/ukljuci".equals(messageText) || "/iskljuci"
					.equals(messageText) || "/meteo".equals(messageText) || "/pomoc".equals(messageText));
		}
		return false;
	}

	/**
	 * Is received message level 1
	 */
	private static boolean isValidLvl2Message(TelegramMessage lvl1Message, TelegramMessage lvl2Message) {
		if (lvl1Message != null && lvl2Message != null) {
			String lvl1MessageText = lvl1Message.getText();
			String lvl2MessageText = lvl2Message.getText();
			if ("/timer".equals(lvl1MessageText)) {
				return ("/off".equals(lvl2MessageText) || (lvl2MessageText.length() > 1 && PropertiesUtil.isValidTimerString(
						lvl2MessageText.substring(1))));
			}
		}
		return false;
	}

	/**
	 * Is message older then number of seconds provided as argument
	 */
	private static boolean isMessageOlderThan(TelegramMessage message, int numberOfSeconds) {
		if (message == null) {
			return true;
		}
		long diff = (System.currentTimeMillis() / 1000L) - message.getDate();
		return diff > numberOfSeconds;
	}

	/**
	 * Returns text of previously received message if exists
	 */
	private static String getPreviousMessageText() {
		return previousMessage != null ? previousMessage.getText() : null;
	}

	public static void sendMessage(String messageText) {
		Message message = new Message(chat, sender, messageText);
		sendMessage(message);
	}

	/**
	 * returns meteo info message
	 */
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

	/**
	 * returns timer message
	 */
	private static String getTimerMessage() {
		if (PropertiesUtil.getTimer() != null) {
			return "Automatsko uključivanje je postavljeno u " + PropertiesUtil.getTimer() + ". Za promjenu, pošalji novo vrijeme u formatu '/HH:MM' (npr. /08:00) ili '/off' za isključivanje timera.";
		}
		return "Automatsko uključivanje još nije postavljeno. Za promjenu, pošalji novo vrijeme automatskog uključivanja u formatu '/HH:MM' (npr. /08:00).";
	}

}
