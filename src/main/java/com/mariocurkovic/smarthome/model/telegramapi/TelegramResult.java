package com.mariocurkovic.smarthome.model.telegramapi;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"update_id", "message"})
public class TelegramResult {

	@JsonProperty("update_id")
	private Integer updateId;
	@JsonProperty("message")
	private TelegramMessage message;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("update_id")
	public Integer getUpdateId() {
		return updateId;
	}

	@JsonProperty("update_id")
	public void setUpdateId(Integer updateId) {
		this.updateId = updateId;
	}

	@JsonProperty("message")
	public TelegramMessage getMessage() {
		return message;
	}

	@JsonProperty("message")
	public void setMessage(TelegramMessage message) {
		this.message = message;
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
