package com.mariocurkovic.smarthome.model.telegramapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"message_id", "from", "chat", "date", "text", "entities"})
public class TelegramMessage {

	@JsonProperty("message_id")
	private Integer messageId;
	@JsonProperty("from")
	private TelegramFrom from;
	@JsonProperty("chat")
	private TelegramChat chat;
	@JsonProperty("date")
	private Integer date;
	@JsonProperty("text")
	private String text;
	@JsonProperty("entities")
	private List<TelegramEntity> entities = null;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("message_id")
	public Integer getMessageId() {
		return messageId;
	}

	@JsonProperty("message_id")
	public void setMessageId(Integer messageId) {
		this.messageId = messageId;
	}

	@JsonProperty("from")
	public TelegramFrom getFrom() {
		return from;
	}

	@JsonProperty("from")
	public void setFrom(TelegramFrom from) {
		this.from = from;
	}

	@JsonProperty("chat")
	public TelegramChat getChat() {
		return chat;
	}

	@JsonProperty("chat")
	public void setChat(TelegramChat chat) {
		this.chat = chat;
	}

	@JsonProperty("date")
	public Integer getDate() {
		return date;
	}

	@JsonProperty("date")
	public void setDate(Integer date) {
		this.date = date;
	}

	@JsonProperty("text")
	public String getText() {
		return text;
	}

	@JsonProperty("text")
	public void setText(String text) {
		this.text = text;
	}

	@JsonProperty("entities")
	public List<TelegramEntity> getEntities() {
		return entities;
	}

	@JsonProperty("entities")
	public void setEntities(List<TelegramEntity> entities) {
		this.entities = entities;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

}
