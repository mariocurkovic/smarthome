package com.mariocurkovic.smarthome;

import com.mariocurkovic.smarthome.model.Chat;
import com.mariocurkovic.smarthome.model.Client;
import com.mariocurkovic.smarthome.model.Message;
import com.mariocurkovic.smarthome.model.Weather;
import com.mariocurkovic.smarthome.util.TelegramUtil;
import com.mariocurkovic.smarthome.util.WebParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleApp {

	private static Logger logger = LoggerFactory.getLogger(SimpleApp.class);

	public static void main(String[] args) {

		logger.info("Starting app...");

		// TODO: Read from properties
		Chat chat = new Chat("-472738592", "smarthome", true);
		Client sender = new Client("smarthome_mario_bot", "1442601388:AAGEOvA3K91vq9tClgFweMaKccmgzOCGlPQ");

		Weather temperature = WebParser.getLocalWeather("Sinj");

		if (temperature.getTemperature() != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("Meteo podatci za " + temperature.getMeteoStation());
			sb.append(" u " + temperature.getLastUpdatedTime() + ":");
			sb.append("  Temperatura " + temperature.getTemperature() + "°");
			sb.append(", Tlak " + temperature.getPressure() + "hPa");
			sb.append(", Vlažnost: " + temperature.getHumidity() + "%");
			sb.append(".");
			Message message = new Message(chat, sender, sb.toString());
			TelegramUtil.sendMessage(message);
		}

	}

}
