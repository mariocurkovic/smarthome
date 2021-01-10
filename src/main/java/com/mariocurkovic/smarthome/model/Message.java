package com.mariocurkovic.smarthome.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Message {

	private Chat chat;
	private Client sender;
	private String text;

	public Message( Chat chat, Client sender, String text) {
		this.chat = chat;
		this.sender = sender;
		this.text = text;
	}



}
