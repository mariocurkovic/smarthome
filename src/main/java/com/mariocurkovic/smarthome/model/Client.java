package com.mariocurkovic.smarthome.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Client {

	private String name;
	private String token;

	public Client(String name, String token) {
		this.name = name;
		this.token = token;
	}

}
