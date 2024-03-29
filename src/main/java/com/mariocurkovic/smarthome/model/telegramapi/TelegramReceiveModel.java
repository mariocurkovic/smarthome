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
@JsonPropertyOrder({"ok", "result"})
public class TelegramReceiveModel {

	@JsonProperty("ok")
	private Boolean ok;
	@JsonProperty("result")
	private List<TelegramResult> result = null;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("ok")
	public Boolean getOk() {
		return ok;
	}

	@JsonProperty("ok")
	public void setOk(Boolean ok) {
		this.ok = ok;
	}

	@JsonProperty("result")
	public List<TelegramResult> getResult() {
		return result;
	}

	@JsonProperty("result")
	public void setResult(List<TelegramResult> result) {
		this.result = result;
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
