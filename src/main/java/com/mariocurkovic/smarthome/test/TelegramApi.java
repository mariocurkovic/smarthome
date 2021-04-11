package com.mariocurkovic.smarthome.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mariocurkovic.smarthome.model.telegramapi.TelegramMessage;
import com.mariocurkovic.smarthome.model.telegramapi.TelegramReceiveModel;
import com.mariocurkovic.smarthome.model.telegramapi.TelegramResult;
import com.mariocurkovic.smarthome.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TelegramApi {

	private static final Logger logger = LoggerFactory.getLogger(TelegramApi.class);

	private static int iteration = 0;

	public static List<TelegramMessage> getMessages() {
		String response;

		if (iteration == 0) {
			response = FileUtil.readFromResources("test/status-message.json");
		} else if (iteration == 1) {
			response = FileUtil.readFromResources("test/empty-message.json");
		} else if (iteration == 2) {
			response = FileUtil.readFromResources("test/empty-message.json");
		} else {
			response = FileUtil.readFromResources("test/two-status-messages.json");
		}
		iteration++;

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			TelegramReceiveModel telegramReceiveModel = objectMapper.readValue(response, TelegramReceiveModel.class);
			if (telegramReceiveModel.getOk() != null && telegramReceiveModel.getResult() != null && !telegramReceiveModel.getResult().isEmpty()) {
				return telegramReceiveModel.getResult().stream().map(TelegramResult::getMessage).collect(Collectors.toList());
			}
		} catch (Exception e) {
			logger.error("Unable to get updates. " + e.getMessage());
		}
		return new ArrayList<>();
	}


}
