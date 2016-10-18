package com.doohh.nn;

import java.io.File;
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
	
	Properties loadTaskProp() {
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
		try {
			Thread.sleep(1000);
			lock.release();
			channel.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// remove confFile after reading it'
		//System.out.println("delete file: " + confFile.getName());
		confFile.delete();
		
		return this.props;
	}	

	private boolean checkFile(File file) {
		try {
			props = PropFactory.getInstance(file.getName()).getProperties();
			if (props == null) {
				return false;
			} else {
				this.channel = new RandomAccessFile(file, "rw").getChannel();
				this.lock = this.channel.lock();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
}
