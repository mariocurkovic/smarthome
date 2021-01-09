package com.mariocurkovic.smarthome.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mariocurkovic.smarthome.model.Chat;
import com.mariocurkovic.smarthome.model.Client;
import com.mariocurkovic.smarthome.model.Message;
import com.mariocurkovic.smarthome.model.Weather;
import com.mariocurkovic.smarthome.model.telegramapi.ReceiveModel;
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

	// TODO: Read from properties
	private static final Chat chat = new Chat("-472738592", "smarthome", true);
	private static final Client sender = new Client("smarthome_mario_bot", "1442601388:AAGEOvA3K91vq9tClgFweMaKccmgzOCGlPQ");

	private static final Logger logger = LoggerFactory.getLogger(TelegramUtil.class);
	private static Integer lastUpdateId = -1;

	private static String previousMessage;

	public static void checkMessages(boolean first) {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
										 .uri(URI.create(
												 "https://api.telegram.org/bot1442601388:AAGEOvA3K91vq9tClgFweMaKccmgzOCGlPQ/getUpdates"))
										 .build();
		try {
			HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
			ObjectMapper objectMapper = new ObjectMapper();
			ReceiveModel receiveModel = objectMapper.readValue(response.body().toString(), ReceiveModel.class);
			com.mariocurkovic.smarthome.model.telegramapi.Message lastReceivedMessage = receiveModel.getResult()
																									.get(receiveModel.getResult()
																													 .size() - 1)
																									.getMessage();
			Integer lastReceivedMessageUpdateId = receiveModel.getResult().get(receiveModel.getResult().size() - 1).getUpdateId();

			if (receiveModel.getResult() != null && receiveModel.getResult().size() > 0 && !lastReceivedMessageUpdateId.equals(
					lastUpdateId)) {

				if (first) {
					lastUpdateId = lastReceivedMessageUpdateId;
					return;
				}

				logger.info("Received new message from " + lastReceivedMessage.getFrom()
																			  .getFirstName() + " " + lastReceivedMessage.getFrom()
																														 .getLastName() + ": " + lastReceivedMessage
						.getText());
				lastUpdateId = lastReceivedMessageUpdateId;
				handleMessage(lastReceivedMessage.getText());
			}
		} catch (IOException | InterruptedException e) {
			// do nothing
		} catch (NullPointerException e) {
			logger.error("Unable to parse message.");
		}

	}

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

	private static void handleMessage(String receivedMessage) {
		if ("/timer".equals(previousMessage)) {
			String messageText;
			if ("/off".equals(receivedMessage)) {
				PropertiesUtil.timer = null;
				PropertiesUtil.updateStartupProperties();
				messageText = "Automatsko uključivanje grijanja je poništeno.";
			} else if (PropertiesUtil.setTimer(receivedMessage.substring(1))) {
				PropertiesUtil.updateStartupProperties();
				messageText = "Postavljeno je automatsko uključivanje grijanja u " + PropertiesUtil.getTimer();
			} else {
				messageText = "Nije uspjelo postavljanje timera automatskog uključivanja grijanja. Očekivani format vremena: '/HH:MM' (npr. /08:00)" + PropertiesUtil
						.getTimer();
			}
			Message message = new Message(chat, sender, messageText);
			sendMessage(message);
		} else if ("/timer".equals(receivedMessage)) {
			String messageText;
			if (PropertiesUtil.getTimer() != null) {
				messageText = "Automatsko uključivanje je postavljeno u " + PropertiesUtil.getTimer() + ". Za promjenu, pošalji novo vrijeme u formatu '/HH:MM' (npr. /08:00) ili /off za isključivanje timera.";
			} else {
				messageText = "Automatsko uključivanje još nije postavljeno. Za promjenu, pošalji novo vrijeme automatskog uključivanja u formatu '/HH:MM' (npr. /08:00).";
			}
			Message message = new Message(chat, sender, messageText);
			sendMessage(message);
		} else if ("/status".equals(receivedMessage)) {
			String messageText = "Grijanje je " + (GpioUtil.isOn("00") ? "uključeno" : "isključeno") + ".";
			Message message = new Message(chat, sender, messageText);
			sendMessage(message);
		} else if ("/ukljuci".equals(receivedMessage)) {
			boolean result = GpioUtil.turnOn("00");
			String messageText = "Grijanje je " + (result ? "uključeno" : "isključeno") + ".";
			Message message = new Message(chat, sender, messageText);
			sendMessage(message);
		} else if ("/iskljuci".equals(receivedMessage)) {
			boolean result = GpioUtil.turnOff("00");
			String messageText = "Grijanje je " + (result ? "isključeno" : "uključeno") + ".";
			Message message = new Message(chat, sender, messageText);
			sendMessage(message);
		} else if ("/temperature".equals(receivedMessage)) {
			Weather weather = WebParser.getLocalWeather("Sinj");
			if (weather.getMeteoStation() != null) {
				StringBuilder sb = new StringBuilder();
				sb.append("Meteo podatci za ").append(weather.getMeteoStation());
				sb.append(" (").append(weather.getLastUpdatedTime()).append("):");
				sb.append("  Temperatura ").append(weather.getTemperature()).append("°");
				sb.append(", Tlak ").append(weather.getPressure()).append("hPa");
				sb.append(", Vlažnost: ").append(weather.getHumidity()).append("%");
				sb.append(".");
				Message message = new Message(chat, sender, sb.toString());
				sendMessage(message);
			}
		} else if ("/pomoc".equals(receivedMessage)) {
			StringBuilder sb = new StringBuilder();
			sb.append("Dostupne komande:\n");
			sb.append("/status - vraća status grijanja (uključeno/isključeno)\n");
			sb.append("/ukljuci - uključuje grijanje\n");
			sb.append("/iskljuci - isključuje grijanje\n");
			sb.append("/temperature - vraća meteo podatke za lokaciju (Sinj)\n");
			sb.append("/timer - omogućuje postavljanje automatskog uključivanja grijanja\n");
			sb.append("/pomoc - prikazuje dostupne komande");
			Message message = new Message(chat, sender, sb.toString());
			sendMessage(message);
		}
		previousMessage = receivedMessage;
	}


}
