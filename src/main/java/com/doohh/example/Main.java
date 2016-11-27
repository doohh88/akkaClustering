package com.doohh.example;

import com.doohh.akkaClustering.deploy.SubmitMain;
import com.doohh.akkaClustering.master.MasterMain;
import com.doohh.akkaClustering.worker.WorkerMain;

public class Main {
	public static void main(String[] args) {
		int nWorker = 4;
		MasterMain.main(args);
		for (int i = 0; i < nWorker; i++)
			new WorkerMain().main(args);
		new SubmitMain().main(args);
	}
}
