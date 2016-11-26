package example;

import java.util.Random;
import java.util.concurrent.CyclicBarrier;

public class CyclicBarrierEX {
	private final static int THREADS = 5;
	private static CyclicBarrier cyclicBarrier = new CyclicBarrier(THREADS);
	
	public static class RandomSleepRunnable implements Runnable {
		private int id = 0;
		private static Random random = new Random(System.currentTimeMillis());
		
		public RandomSleepRunnable(int id) {
			this.id = id;
		}
		
		@Override
		public void run() {
			System.out.println("Thread(" + id + ") : Start.");
			int delay = random.nextInt(1001) + 1000;
			try {
				System.out.println("Thread(" + id + ") : Sleep " + delay + "ms");
				Thread.sleep(delay);
				System.out.println("Thread(" + id + ") : End Sleep");
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			try {
				cyclicBarrier.await();
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("Thread(" + id + ") : End.");
			
		}
	}
	
	public static void main(String[] args) {
		for(int i = 0;i < THREADS; i++){
			new Thread(new RandomSleepRunnable(i)).start();
		}
	}
}
