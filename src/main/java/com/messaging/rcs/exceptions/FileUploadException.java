
package com.messaging.rcs.exceptions;

import org.springframework.http.HttpStatus;

/**
 * The Class FileUploadException.
 */
public class FileUploadException extends BaseException {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -803371975107006907L;

  /** The Constant DEFAULT_MESSAGE. */
  private static final String DEFAULT_MESSAGE = "File could not be uploaded due to improper data ";

  /** The Constant DEFAULT_HTTP_STATUS. */
  private static final HttpStatus DEFAULT_HTTP_STATUS = HttpStatus.CONFLICT;

  /**
   * Instantiates a new file upload exception.
   */
  public FileUploadException() {
    this(DEFAULT_HTTP_STATUS, DEFAULT_MESSAGE);
  }

  /**
   * Instantiates a new file upload exception.
   *
   * @param httpStatus the http status
   * @param message the message
   */
  public FileUploadException(HttpStatus httpStatus, String message) {
    super(httpStatus, DEFAULT_HTTP_STATUS, message, DEFAULT_MESSAGE);
  }

  /**
   * Instantiates a new file upload exception.
   *
   * @param message the message
   */
  public FileUploadException(String message) {
    this(DEFAULT_HTTP_STATUS, message);
  }

}
