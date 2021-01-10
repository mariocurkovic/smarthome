package com.mariocurkovic.smarthome.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

public class LogUtil {

	/**
	 * returns list of available log files
	 */
	public static String getListOfLogFiles() {
		try {
			StringBuilder sb = new StringBuilder();
			String[] fileNames;
			File f = new File("./logs");
			fileNames = f.list();
			if (fileNames == null) {
				return "No log files found.";
			}
			Arrays.sort(fileNames, Collections.reverseOrder(String.CASE_INSENSITIVE_ORDER));
			int i = 0;
			for (String fileName : fileNames) {
				if (fileName.contains(".log")) {
					if (i > 0) {
						sb.append("<br/>");
					}
					sb.append(fileName);
					i++;
				}
			}
			return sb.toString();
		} catch (NullPointerException e) {
			// do nothing
		}
		return "No log files found.";
	}

	/**
	 * returns log file content
	 */
	public static String getLogFileContent(String fileName) {
		if (fileName == null) {
			return "No log file found.";
		}
		try {
			String content = Files.readString(Path.of("./logs/" + fileName));
			content = content.replaceAll("\n", "<br/>");
			content = prependLogFileStyle() + "<code>" + content + "</code>";
			return content;
		} catch (IOException e) {
			// do nothing
		}
		return "No log file found.";
	}

	/**
	 * adds css style to printed log file
	 */
	private static String prependLogFileStyle() {
		try {
			InputStream inputStream = LogUtil.class.getClassLoader().getResourceAsStream("static/css/log.css");
			String css = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines()
																									   .collect(Collectors.joining(
																											   "\n"));
			return "<style>" + css + "</style>";
		} catch (Exception e) {
			// do nothing
		}
		return "";
	}

}
