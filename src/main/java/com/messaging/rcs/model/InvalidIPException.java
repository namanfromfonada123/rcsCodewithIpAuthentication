package com.messaging.rcs.model;

public class InvalidIPException extends Exception {
	private String parameter;
	 public static InvalidIPException createWith(String parameter) {
	        return new InvalidIPException(parameter);
	    }

	    public InvalidIPException(String parameter) {
	        this.parameter = parameter;
	    }


	    @Override
	    public String getMessage() {
	        return "Request is not from valid IP.";
	    }
	}
