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

	private static final List<String> autoProvisionPins = Collections.singletonList(PropertiesUtil.getRelayPosition());

	/**
	 * starts GPIO controller
	 */
	public static void init() {
		logger.info("Starting GPIO controller...");
		gpio = GpioFactory.getInstance();
		logger.info("Provisioning pins...");
		for (String position : autoProvisionPins) {
			provisionDigitalOutputPin(position);
		}
		printProvisionedPins();
	}

	/**
	 * turns on relay
	 */
	public static boolean turnOn(String position) {
		if (!isOn(position)) {
			GpioPinDigitalOutput provisionedPinByPosition = (GpioPinDigitalOutput) getProvisionedPinByPosition(position);
			provisionedPinByPosition.low();
			PropertiesUtil.setInitialStatus("ON");
			PropertiesUtil.updateStartupProperties();
			logger.info("Heating turned ON.");
		}
		return isOn(position);
	}

	/**
	 * turns off relay
	 */
	public static boolean turnOff(String position) {
		if (isOn(position)) {
			GpioPinDigitalOutput provisionedPinByPosition = (GpioPinDigitalOutput) getProvisionedPinByPosition(position);
			provisionedPinByPosition.high();
			PropertiesUtil.setInitialStatus("OFF");
			PropertiesUtil.updateStartupProperties();
			logger.info("Heating turned OFF.");
		}
		return !isOn(position);
	}

	/**
	 * returns relay status
	 */
	public static boolean isOn(String position) {
		GpioPinDigitalOutput provisionedPinByPosition = (GpioPinDigitalOutput) getProvisionedPinByPosition(position);
		return provisionedPinByPosition != null && provisionedPinByPosition.isLow();
	}

	/**
	 * provisions digital output pin
	 */
	private static void provisionDigitalOutputPin(String position) {
		Pin pinByPosition = getPinByPosition(position);
		if (pinByPosition != null) {
			logger.info("Provisioning pin GPIO_" + position + "...");
			GpioPinDigitalOutput retVal = gpio.provisionDigitalOutputPin(pinByPosition,
																		 "GPIO_" + position,
																		 ("ON".equals(PropertiesUtil.getInitialStatus())
																		  ? PinState.LOW
																		  : PinState.HIGH));
			retVal.setShutdownOptions(true, PinState.LOW);
		} else {
			logger.error("Error provisioning pin on position " + position);
		}
	}

	/**
	 * returns pin by position
	 */
	private static Pin getPinByPosition(String position) {
		switch (position) {
			case "00":
				return RaspiPin.GPIO_00;
			case "01":
				return RaspiPin.GPIO_01;
			case "02":
				return RaspiPin.GPIO_02;
			case "03":
				return RaspiPin.GPIO_03;
			case "04":
				return RaspiPin.GPIO_04;
			case "05":
				return RaspiPin.GPIO_05;
			case "06":
				return RaspiPin.GPIO_06;
			case "07":
				return RaspiPin.GPIO_07;
			case "08":
				return RaspiPin.GPIO_08;
			case "09":
				return RaspiPin.GPIO_09;
			case "10":
				return RaspiPin.GPIO_10;
			case "11":
				return RaspiPin.GPIO_11;
			case "12":
				return RaspiPin.GPIO_12;
			case "13":
				return RaspiPin.GPIO_13;
			case "14":
				return RaspiPin.GPIO_14;
			case "15":
				return RaspiPin.GPIO_15;
			case "16":
				return RaspiPin.GPIO_16;
			case "17":
				return RaspiPin.GPIO_17;
			case "18":
				return RaspiPin.GPIO_18;
			case "19":
				return RaspiPin.GPIO_19;
			case "20":
				return RaspiPin.GPIO_20;
			case "21":
				return RaspiPin.GPIO_21;
			case "22":
				return RaspiPin.GPIO_22;
			case "23":
				return RaspiPin.GPIO_23;
			case "24":
				return RaspiPin.GPIO_24;
			case "25":
				return RaspiPin.GPIO_25;
			case "26":
				return RaspiPin.GPIO_26;
			case "27":
				return RaspiPin.GPIO_27;
			case "28":
				return RaspiPin.GPIO_28;
			case "29":
				return RaspiPin.GPIO_29;
			case "30":
				return RaspiPin.GPIO_30;
			case "31":
				return RaspiPin.GPIO_31;
			default:
				return null;
		}
	}

	/**
	 * returns provisioned pin by position
	 */
	private static GpioPin getProvisionedPinByPosition(String position) {
		for (GpioPin pin : gpio.getProvisionedPins()) {
			if (("GPIO_" + position).equals(pin.getName())) {
				return pin;
			}
		}
		return null;
	}

	/**
	 * prints provisioned pins
	 */
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