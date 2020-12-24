package com.mariocurkovic.smarthome;

public class App {

	public static void main(String[] args) {

		// TODO: Read from properties
		Chat chat = new Chat("-472738592", "smarthome", true);
		Client sender = new Client("smarthome_mario_bot", "1442601388:AAGEOvA3K91vq9tClgFweMaKccmgzOCGlPQ");

		Temperature temperature = WebParser.getLocalTemperature("Sinj");

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
