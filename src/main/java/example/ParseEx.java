package example;

import org.kohsuke.args4j.Option;

public class ParseEx {
	@Option(name="--batchSize",usage="batchSize",aliases = "-b")
	static int batchSize = 500;
    @Option(name="--nEpochs",usage="nEpochs",aliases = "-e")
    static int nEpochs = 1;
	
	public static void main(String[] args) {
		ParseEx p = new ParseEx();
		Util.parseArgs(args, p);
		System.out.println(p.batchSize);
		System.out.println(p.nEpochs);
		
	}
}
