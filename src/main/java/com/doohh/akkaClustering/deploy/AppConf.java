package com.doohh.akkaClustering.deploy;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

import com.doohh.akkaClustering.util.Node;

import akka.actor.Address;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class AppConf implements Serializable {
	String hostIP;
	String port;
	String jarPath;
	String classPath;
	int nMaster;
	int nWorker;
	String[] args;

	// decide in build
	String masterURL;
	File jarFile;

	// decide in runtime
	String role;
	int roleIdx;

	public void setRole(String role) {
		this.role = role;
	}

	public void setRoleIdx(int roleIdx) {
		this.roleIdx = roleIdx;
	}

	private AppConf(Builder builder) {
		hostIP = builder.hostIP;
		port = builder.port;
		jarPath = builder.jarPath;
		classPath = builder.classPath;
		nMaster = builder.nMaster;
		nWorker = builder.nWorker;
		args = builder.args;
		masterURL = builder.masterURL;
		jarFile = builder.jarFile;
	}

	public static class Builder {
		String hostIP = "127.0.0.1";
		String port = "2551";
		String jarPath = null;
		String classPath = null;
		int nMaster = 1;
		int nWorker = 1;
		String[] args = null;

		String masterURL = null;
		File jarFile = null;

		public Builder() {
		};

		public Builder hostIP(String hostIP) {
			this.hostIP = hostIP;
			return this;
		}

		public Builder port(String port) {
			this.port = port;
			return this;
		}

		public Builder jarPath(String jarPath) {
			this.jarPath = jarPath;
			return this;
		}

		public Builder classPath(String classPath) {
			this.classPath = classPath;
			return this;
		}

		public Builder nMaster(int nMaster) {
			this.nMaster = nMaster;
			return this;
		}

		public Builder nWorker(int nWorker) {
			this.nWorker = nWorker;
			return this;
		}

		public Builder args(String[] args) {
			this.args = args;
			return this;
		}

		public AppConf build() {
			this.masterURL = "akka.tcp://deepDist@" + this.hostIP + ":" + this.port + "/user/master";
			this.jarFile = new File(this.jarPath);
			return new AppConf(this);
		}

	}
}
