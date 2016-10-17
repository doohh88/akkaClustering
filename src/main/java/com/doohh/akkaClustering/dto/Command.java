package com.doohh.akkaClustering.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Command<T> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String command;
	private T data;

	public Command setCommand(String cmd) {
		this.command = cmd;
		return this;
	}

	public Command setData(T data) {
		this.data = data;
		return this;
	}
}
