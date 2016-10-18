package example;

import com.doohh.akkaClustering.master.MasterMain;
import com.doohh.akkaClustering.worker.WorkerMain;

public class Main {
	public static void main(String[] args) {
		int nWorker = 3;
		MasterMain.main(args);
		for (int i = 0; i < nWorker; i++) {
			(new WorkerMain()).main(args);
			//WorkerMain.main(args);
		}
	}
}
