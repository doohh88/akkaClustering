package com.doohh.nn;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Properties;

import com.doohh.akkaClustering.util.PropFactory;
import com.doohh.akkaClustering.util.Util;

public class LoadTaskProp {
	private Properties props;
	private FileLock lock = null;
	private FileChannel channel = null;

	public Properties loadTaskProp() {
		// read confFile
		File confFile = null;
		String path = Util.getHomeDir() + "/conf";
		File[] fileList = Util.getFileList(path);
		ArrayList<File> confFiles = new ArrayList<File>();
		for (File file : fileList) {
			if (file.getName().contains("task_")) {
				try {
					if (checkFile(file)) {
						confFile = file;
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		unlockFile(confFile);
		props = PropFactory.getInstance(confFile.getName()).getProperties();

		// remove confFile after reading it'
		// System.out.println("delete file: " + confFile.getName());
		confFile.delete();
		return this.props;
	}

	private boolean checkFile(File file) {
		try {
			this.channel = new RandomAccessFile(file, "rw").getChannel();
			this.lock = this.channel.tryLock();
			if (lock == null) {
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	public void unlockFile(File file) {
		try {
			if (lock != null) {
				Thread.sleep(1000);
				lock.release();
				channel.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
