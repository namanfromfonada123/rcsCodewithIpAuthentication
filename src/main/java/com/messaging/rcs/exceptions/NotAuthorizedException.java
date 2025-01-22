
package com.messaging.rcs.exceptions;

import org.springframework.http.HttpStatus;

/**
 * The Class NotAuthorizedException.
 */
public class NotAuthorizedException extends BaseException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7201815216491899773L;

	/** The Constant DEFAULT_MESSAGE. */
	private static final String DEFAULT_MESSAGE = "Not Authorized !";

	/** The Constant DEFAULT_HTTP_STATUS. */
	private static final HttpStatus DEFAULT_HTTP_STATUS = HttpStatus.UNAUTHORIZED;

	/**
	 * Instantiates a new not authorized exception.
	 */
	public NotAuthorizedException() {
		this(DEFAULT_HTTP_STATUS, DEFAULT_MESSAGE);
	}

	/**
	 * Instantiates a new not authorized exception.
	 *
	 * @param httpStatus the http status
	 * @param message    the message
	 */
	public NotAuthorizedException(HttpStatus httpStatus, String message) {
		super(httpStatus, DEFAULT_HTTP_STATUS, message, DEFAULT_MESSAGE);

	}

	/**
	 * Instantiates a new not authorized exception.
	 *
	 * @param message the message
	 */
	public NotAuthorizedException(String message) {
		this(DEFAULT_HTTP_STATUS, message);
	}

}
