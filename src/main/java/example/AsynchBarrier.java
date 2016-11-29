package example;

import com.doohh.akkaClustering.dto.DistInfo;
import com.doohh.akkaClustering.worker.Controller;

public class AsynchBarrier {
	public static void main(String[] args) {
		DistInfo distInfo = null;
		if (distInfo.getRole().equals("slave")){ 
			if (distInfo.getRoleIdx() != 0){
				Controller.barrier(distInfo, "slave");
				System.out.println("hihi");
			}
			if(distInfo.getRoleIdx() == 0)
				Controller.barrier(distInfo, "slave");
			System.out.println("helo");
		}
	}
}
