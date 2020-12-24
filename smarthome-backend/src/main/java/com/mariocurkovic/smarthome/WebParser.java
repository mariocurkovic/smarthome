package com.mariocurkovic.smarthome;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class WebParser {

	public static Temperature getLocalTemperature(String meteoStation) {
		Temperature temperature = new Temperature();

		try {
			Document doc = Jsoup.connect("https://pljusak.com/trenutno-vrijeme-tablica.php").get();
			Elements rows = doc.select("table.tablesorter tr");

			for (Element row : rows) {
				Elements columns = row.select("td");
				if (columns.size() > 20) {
					if (meteoStation.equals(columns.get(2).text().trim())) {
						temperature.setMeteoStation(meteoStation);
						temperature.setLastUpdatedTime(columns.get(4).text().trim());
						temperature.setTemperature(columns.get(5).text().trim());
						temperature.setPressure(columns.get(15).text().trim());
						temperature.setHumidity(columns.get(17).text().trim());
					}
				}
			}


		} catch (IOException e) {
			e.printStackTrace();
		}
		return temperature;
	}


}
