package com.doohh.akkaClustering.deploy;


import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserAppConf implements Serializable{
	String packagePath;
	String mainClass;	
}
