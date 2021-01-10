package com.mariocurkovic.smarthome.util;

import com.mariocurkovic.smarthome.model.MeteoInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class WebParser {

	/**
	 * returns meteo info for specific station from pljusak.com
	 */
	public static MeteoInfo getMeteoInfo(String meteoStation) {
		MeteoInfo weather = new MeteoInfo();
		if (meteoStation == null) {
			return weather;
		}
		try {
			Document doc = Jsoup.connect("https://pljusak.com/trenutno-vrijeme-tablica.php").get();
			Elements rows = doc.select("table.tablesorter tr");

			for (Element row : rows) {
				Elements columns = row.select("td");
				if (columns.size() > 20) {
					if (meteoStation.equals(columns.get(2).text().trim())) {
						weather.setStation(meteoStation);
						weather.setLastUpdatedTime(columns.get(4).text().trim());
						weather.setTemperature(columns.get(5).text().trim());
						weather.setPressure(columns.get(15).text().trim());
						weather.setHumidity(columns.get(17).text().trim());
					}
				}
			}


		} catch (IOException e) {
			e.printStackTrace();
		}
		return weather;
	}


}
