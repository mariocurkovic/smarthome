package com.mariocurkovic.smarthome.util;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class GpioUtil {

	private static final Logger logger = LoggerFactory.getLogger(GpioUtil.class);

	private static GpioController gpio = GpioFactory.getInstance();

	private static List<String> autoProvisionPins = Collections.singletonList("00");

	public static void startGpioController() {
		logger.info("Starting GPIO controller...");
		gpio = GpioFactory.getInstance();
		logger.info("GPIO controller started successfully");
		logger.info("Provisioning pins...");
		for (String position : autoProvisionPins) {
			provisionDigitalOutputPin(position);
		}
		printProvisionedPins();
	}

	public static boolean turnOn(String position) {
		if (!isOn(position)) {
			GpioPinDigitalOutput provisionedPinByPosition = (GpioPinDigitalOutput) getProvisionedPinByPosition(position);
			provisionedPinByPosition.low();
		}
		return isOn(position);
	}

	public static boolean turnOff(String position) {
		if (isOn(position)) {
			GpioPinDigitalOutput provisionedPinByPosition = (GpioPinDigitalOutput) getProvisionedPinByPosition(position);
			provisionedPinByPosition.high();
		}
		return !isOn(position);
	}

	public static boolean isOn(String position) {
		GpioPinDigitalOutput provisionedPinByPosition = (GpioPinDigitalOutput) getProvisionedPinByPosition(position);
		return provisionedPinByPosition != null && provisionedPinByPosition.isLow();
	}

	private static void provisionDigitalOutputPin(String position) {
		Pin pinByPosition = getPinByPosition(position);
		if (pinByPosition != null) {
			logger.info("Provisioning pin GPIO_" + position + "...");
			GpioPinDigitalOutput retVal = gpio.provisionDigitalOutputPin(pinByPosition, "GPIO_" + position, PinState.HIGH);
			retVal.setShutdownOptions(true, PinState.LOW);
			logger.info("GPIO_" + position + " provisioned successfuly.");
		} else {
			logger.error("Error provisioning pin on position " + position);
		}
	}

	private static Pin getPinByPosition(String position) {
		switch (position) {
			case "00":
				return RaspiPin.GPIO_00;
			default:
				return null;
		}
	}

	private static GpioPin getProvisionedPinByPosition(String position) {
		for (GpioPin pin : gpio.getProvisionedPins()) {
			if (("GPIO_" + position).equals(pin.getName())) {
				return pin;
			}
		}
		return null;
	}

	private static void printProvisionedPins() {
		StringBuilder sb = new StringBuilder();
		for (GpioPin pin : gpio.getProvisionedPins()) {
			if (sb.toString().length() != 0) {
				sb.append(", ");
			}
			sb.append(pin.getName());
		}
		logger.info("Provisioned pins: " + sb.toString());
	}

}