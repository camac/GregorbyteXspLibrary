package com.gregorbyte.xsp.example;

import java.io.Serializable;

public class ExampleBean implements Serializable {

	private static final long serialVersionUID = -4533783428834302109L;

	public ExampleBean() {
	
	}
	
	
	public String getMessage() {
		return getAnotherMessage();
	}
	
	private String getAnotherMessage() {
		return "another message!";
	}
	
}
