package com.doohh.akkaClustering.util;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.doohh.akkaClustering.master.MasterMain;

public class Util {
	private static final Logger log = LoggerFactory.getLogger(Util.class);
	
	public static void parseArgs(String[] args, Object obj) {
		CmdLineParser parser = new CmdLineParser(obj);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
        	log.info(e.getMessage());
            parser.printUsage(System.err);
        }
	}
}
