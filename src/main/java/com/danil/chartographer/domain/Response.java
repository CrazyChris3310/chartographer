package com.danil.chartographer.domain;

/**
 * Response is used to be returned by server, containing a message
 */
public class Response {

  private String message;

  public Response() {
  }

  public Response(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
