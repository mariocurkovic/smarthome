package com.mariocurkovic.smarthome;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Temperature {

	private String meteoStation;
	private String temperature;
	private String lastUpdatedTime;
	private String pressure;
	private String humidity;

}
