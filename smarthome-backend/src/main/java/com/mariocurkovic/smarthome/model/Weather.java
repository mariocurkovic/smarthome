package com.mariocurkovic.smarthome.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Weather {

	private String meteoStation;
	private String temperature;
	private String lastUpdatedTime;
	private String pressure;
	private String humidity;

}
