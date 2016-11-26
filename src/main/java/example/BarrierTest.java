package example;

public class BarrierTest {
	public BarrierTest() {
	}

	void run() {
		System.out.println("hello1");
		try {
			this.wait(20);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("hello2");
	}

	public static void main(String[] args) {
		BarrierTest barrierTest = new BarrierTest();
		barrierTest.run();
	}
}
