package com.doohh.akkaClustering.dto;

import java.io.Serializable;

import akka.actor.ActorRef;
import akka.actor.Address;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Node implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// Address address;
	private ActorRef actorRef;
	private boolean proc;
}
