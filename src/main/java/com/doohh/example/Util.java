package com.doohh.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util {
	 private static final Logger log = LoggerFactory.getLogger(Util.class);

	public static void load(String name) {
		try {
			log.info("Trying to load: {}", name);
			System.loadLibrary(name);
		} catch (Throwable e) {
			log.info("Failed: {}", e.getMessage());
			return;
		}
		log.info("success");
	}

}
