package com.mariocurkovic.smarthome.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtil {

	private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

	public static String readFromResources(String filePath) {
		StringBuilder resultStringBuilder = new StringBuilder();
		ClassLoader classLoader = FileUtil.class.getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(filePath);
		try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
			String line;
			while ((line = br.readLine()) != null) {
				resultStringBuilder.append(line).append("\n");
			}
		} catch (IOException e) {
			logger.error("Error reading resource: " + filePath);
			e.printStackTrace();
		}
		return resultStringBuilder.toString();
	}


}
