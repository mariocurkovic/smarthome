package com.mariocurkovic.smarthome;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class TelegramUtil {

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
			System.out.println("Unable to send message: " + message.toString() + ". Response: " + response.statusCode() + " " + response.body());
		}

		return response.statusCode() == 200;

	}


}
