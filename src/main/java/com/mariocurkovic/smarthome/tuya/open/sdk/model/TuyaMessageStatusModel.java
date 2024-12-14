package com.mariocurkovic.smarthome.tuya.open.sdk.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class TuyaMessageStatusModel {

    String code;

    Long t;

    Boolean value;

    @JsonProperty("1")
    String _1;
}
