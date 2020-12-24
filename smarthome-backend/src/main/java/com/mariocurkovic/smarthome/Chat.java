package com.mariocurkovic.smarthome;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Chat {

	private String name;
	private boolean group;
	private String id;

	public Chat(String id, String name, boolean group) {
		this.id = id;
		this.name = name;
		this.group = group;
	}

}
