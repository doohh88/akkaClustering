package com.doohh.akkaClustering.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class NetworkInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private List<String> paramAddr = null;
	private List<String> slaveAddr = null;
	
	public NetworkInfo(){
		paramAddr = new ArrayList<String>();
		slaveAddr = new ArrayList<String>();
	}
}
