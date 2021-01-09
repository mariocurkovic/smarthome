package com.mariocurkovic.smarthome.util;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

@Component
public class CronThread implements DisposableBean, Runnable {

	private static final Logger logger = LoggerFactory.getLogger(CronThread.class);

	private volatile boolean isActive;

	CronThread() {
		logger.info("Starting cron thread...");
		Thread thread = new Thread(this);
		thread.start();
	}

	@SneakyThrows
	@Override
	public void run() {
		boolean first = true;
		isActive = true;
		while (isActive) {

			// Turn on heating if timer reached
			if (PropertiesUtil.isTimeForTimer()) {
				GpioUtil.turnOn("00");
			}

			// Check for new telegram messages
			try {
				TelegramUtil.checkMessages(first);
			} catch (Exception e) {
				// do nothing
			}

			// Sleep 5 seconds before next check
			Thread.sleep(5000);
			first = false;
		}
	}

	@Override
	public void destroy() {
		isActive = false;
	}
}
