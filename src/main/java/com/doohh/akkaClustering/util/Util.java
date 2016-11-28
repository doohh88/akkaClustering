package com.doohh.akkaClustering.util;

import java.io.File;
import java.io.FileWriter;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util {
	private static final Logger log = LoggerFactory.getLogger(Util.class);

	public static String[] parseArgs(String[] args, Object obj, String splitOp) {
		// with parsing by splitOp
		int argsLen = args.length;
		String[] appArgs = null;
		String[] parseArgs = null;

		// parsing
		// ~~~~~ -class clazz를 기준으로 파싱
		for (int i = 0; i < argsLen; i++) {
			// if (args[i].equals("--class") || args[i].equals("-c")) {
			if (args[i].equals(splitOp)) {
				if (argsLen == i + 2) {
					// no appARgs
					parseArgs = args;
					appArgs = new String[0];
				} else {
					System.out.println("argsLen: " + argsLen);
					System.out.println("i: " + i);
					int appArgsLen = argsLen - i - 2;
					int parseArgsLen = i + 2;
					appArgs = new String[appArgsLen];
					System.arraycopy(args, parseArgsLen, appArgs, 0, appArgsLen);
					parseArgs = new String[parseArgsLen];
					System.arraycopy(args, 0, parseArgs, 0, parseArgsLen);
				}
				break;
			}
		}

		CmdLineParser parser = new CmdLineParser(obj);
		try {
			if (parseArgs == null) {
				parser.parseArgument(args);
			} else {
				parser.parseArgument(parseArgs);
			}
		} catch (CmdLineException e) {
			log.info(e.getMessage());
			parser.printUsage(System.err);
		}

		return appArgs;

	}

	public static void parseArgs(String[] args, Object obj) {
		CmdLineParser parser = new CmdLineParser(obj);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			log.info(e.getMessage());
			parser.printUsage(System.err);
		}
	}

	public static void write(String fileName, String str) {
		try {
			File file = new File(fileName);
			FileWriter fw = new FileWriter(file, false);
			fw.write(str);
			fw.flush();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getHomeDir() {
		String homeDir = System.getProperty("user.dir");
		int i = homeDir.lastIndexOf("/bin");
		if (i != -1) {
			homeDir = homeDir.substring(0, i);
		}
		return homeDir;
	}

	public static File[] getFileList(String path) {
		File dirFile = new File(path);
		File[] fileList = dirFile.listFiles();
		return fileList;
	}
	
	public static void load(String name) {
		try {
			log.error("Trying to load: {}", name);
			System.loadLibrary(name);
		} catch (Throwable e) {
			log.error("Failed: {}", e.getMessage());
			return;
		}
		log.error("success");
	}
}
