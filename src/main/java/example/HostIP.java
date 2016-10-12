package example;

import java.net.InetAddress;

public class HostIP {
	public static void main(String[] args) {
		try {
			InetAddress ip = InetAddress.getLocalHost();
			System.out.println(ip.getCanonicalHostName());
			System.out.println(ip.getHostAddress());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
