package com.doohh.akkaClustering.deploy;

import java.io.File;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class AppConf implements Serializable{
	String masterIP;
	String port;
	String jarPath;
	String classPath;	
	int parallelism;
	
	String masterURL;
	File jarFile;
	
	private AppConf(Builder builder){
		masterIP = builder.masterIP;
		port = builder.port;
		jarPath = builder.jarPath;
		classPath = builder.classPath;
		parallelism = builder.parallelism;
		masterURL = builder.masterURL;
		jarFile = builder.jarFile;
	}
	
	public static class Builder {
		String masterIP;
		String port;
		String jarPath;
		String classPath;
		int parallelism;
		
		String masterURL;
		File jarFile;

		
		public Builder() {};
		
		public Builder masterIP(String masterIP){
			this.masterIP = masterIP;
			return this;
		}
		public Builder port(String port){
			this.port = port;
			return this;
		}
		public Builder jarPath(String jarPath){
			this.jarPath = jarPath;
			return this;
		}
		public Builder classPath(String classPath){
			this.classPath = classPath;
			return this;
		}
		public Builder parallelism(int parallelism){
			this.parallelism = parallelism;
			return this;
		}
		
		public AppConf build(){
			this.masterURL = "akka.tcp://deepDist@" + this.masterIP + ":" + this.port + "/user/master";
			this.jarFile = new File(this.jarPath);
			return new AppConf(this);
		}
		
	}
}
