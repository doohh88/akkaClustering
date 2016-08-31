package com.doohh.akkaClustering.deploy;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Data;

@Data
public class SubmitCli {
	Logger log = LoggerFactory.getLogger(SubmitCli.class);

	private String[] args = null;
	private Options options = new Options();
	private String mainClass = null;
	private String packagePath = null;
	private UserAppConf userAppConf = null;
	
	public SubmitCli(String[] args) {
		this.args = args;

		options.addOption("h", "help", false, "show help.");
		options.addOption("j", "jar", true, "Here you can set jar package.");
		options.addOption("c", "class", true, "Here you can set main class.");
		
		parse();
	}

	public void parse() {
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(options, args);

			if (cmd.hasOption("h"))
				help();

			if (cmd.hasOption("j")) {
				packagePath = cmd.getOptionValue("j");
			} else {
				help();
			}
			
			if (cmd.hasOption("c")) {
				mainClass = cmd.getOptionValue("c");
			} else {
				help();
			}

		} catch (ParseException e) {

			help();
		}
		
		userAppConf = new UserAppConf(packagePath, mainClass);
	}

	private void help() {
		HelpFormatter formater = new HelpFormatter();
		formater.printHelp("Main", options);
		System.exit(0);
	}

}
