package com.doohh.akkaClustering.experiments;

import com.doohh.akkaClustering.deploy.SubmitMain;
import com.doohh.akkaClustering.master.MasterMain;
import com.doohh.akkaClustering.worker.WorkerMain;

public class Main {
	public static void main(String[] m_args) {
		int nWorker = 5;
		MasterMain.main(m_args);
		for (int i = 0; i < nWorker; i++)
			new WorkerMain().main(m_args);
		
		// ****************************************************************************
		String[] args = new String[10];
		args[0] = "-m";
		args[1] = "1";
		args[2] = "-w";
		args[3] = "4";
		args[4] = "-j";
		args[5] = "C:/git/akkaClustering/target/akkaClustering-0.0.1-allinone.jar";
		args[6] = "-c";
		args[7] = "com.doohh.akkaClustering.experiments.CifarDistSyncEx";
		args[8] = "-l";
		args[9] = "1";
		//args[10] = "args3";
		// ****************************************************************************
		//new SubmitMain().main(m_args);
	}
}
