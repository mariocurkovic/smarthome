package com.mariocurkovic.smarthome.tuya.open.sdk.model;

import lombok.Getter;

import java.util.List;

@Getter
public class TuyaMessageModel {

    String dataId;

    String devId;

    String productKey;

    List<TuyaMessageStatusModel> status;
}
