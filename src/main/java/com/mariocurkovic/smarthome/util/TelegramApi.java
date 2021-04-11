package com.mariocurkovic.smarthome.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mariocurkovic.smarthome.model.Chat;
import com.mariocurkovic.smarthome.model.Client;
import com.mariocurkovic.smarthome.model.Message;
import com.mariocurkovic.smarthome.model.telegramapi.TelegramChat;
import com.mariocurkovic.smarthome.model.telegramapi.TelegramMessage;
import com.mariocurkovic.smarthome.model.telegramapi.TelegramReceiveModel;
import com.mariocurkovic.smarthome.model.telegramapi.TelegramResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TelegramApi {

	private static final Logger logger = LoggerFactory.getLogger(TelegramApi.class);

	private static final Client sender = new Client(PropertiesUtil.getTelegramClientName(), PropertiesUtil.getTelegramClientToken());

	public static List<TelegramMessage> getMessages() {
		URI uri = URI.create("https://api.telegram.org/bot" + PropertiesUtil.getTelegramClientToken() + "/getUpdates");
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().uri(uri).build();
		try {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			ObjectMapper objectMapper = new ObjectMapper();
			TelegramReceiveModel telegramReceiveModel = objectMapper.readValue(response.body(), TelegramReceiveModel.class);
			if (telegramReceiveModel.getOk() != null && telegramReceiveModel.getResult() != null && !telegramReceiveModel.getResult().isEmpty()) {
				return telegramReceiveModel.getResult().stream().map(TelegramResult::getMessage).collect(Collectors.toList());
			}
		} catch (Exception e) {
			logger.error("Unable to get updates. " + e.getMessage());
			return null;
		}
		return new ArrayList<>();
	}

	/**
	 * sends message to telegram chat
	 */
	public static boolean sendMessage(Message message) {
		try {

			HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).version(HttpClient.Version.HTTP_2).build();

			UriBuilder builder = UriBuilder.fromUri("https://api.telegram.org")
										   .path("/{token}/sendMessage")
										   .queryParam("chat_id", message.getChat().getId())
										   .queryParam("parse_mode", "HTML")
										   .queryParam("text", message.getText());

			HttpRequest request = HttpRequest.newBuilder().GET().uri(builder.build("bot" + PropertiesUtil.getTelegramClientToken())).timeout(Duration.ofSeconds(5)).build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() != 200) {
				System.out.println("Unable to send message: " + message.toString() + ". Response: " + response.statusCode() + " " + response.body());
			}

			return response.statusCode() == 200;

		} catch (IOException | InterruptedException e) {
			logger.error("Unable to send message: " + message.toString() + ". " + e.getMessage());
		}

		return false;
	}

	public static boolean deleteMessage(TelegramMessage telegramMessage) {
		try {

			HttpClient client = HttpClient.newHttpClient();
			UriBuilder builder = UriBuilder.fromUri("https://api.telegram.org")
										   .path("/{token}/deleteMessage")
										   .queryParam("chat_id", telegramMessage.getChat().getId())
										   .queryParam("message_id", telegramMessage.getMessageId());

			HttpRequest request = HttpRequest.newBuilder().uri(builder.build("bot" + PropertiesUtil.getTelegramClientToken())).build();
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			boolean success = (response.statusCode() == 200 || (response.statusCode() == 400 && response.body().contains("Bad Request: message to delete not found")));
			if (!success) {
				logger.error("Unable to delete message " + telegramMessage.getText() + " (" + response.statusCode() + ").");
			}
			return success;

		} catch (Exception e) {
			logger.error("Unable to delete message " + telegramMessage.getText() + ". " + e.getMessage());
		}

		return false;

	}

	public static void sendMessage(String messageText, TelegramChat chat) {
		Chat tmpChat = new Chat(String.valueOf(chat.getId()), chat.getTitle(), true);
		Message message = new Message(tmpChat, sender, messageText);
		TelegramApi.sendMessage(message);
	}

}
