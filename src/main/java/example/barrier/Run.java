package example.barrier;

public class Run {
	public static void main(String[] args) {
		Main.main(args);
		for (int i = 0; i < 3; i++)
			Sub.main(new String[]{"-n", String.valueOf(i)});
	}
}
