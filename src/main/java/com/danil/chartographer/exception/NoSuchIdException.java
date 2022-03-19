package com.danil.chartographer.exception;

/**
 * {@code NoSuchIdException} is thrown when id of the image hasn't been found in the folder
 */
public class NoSuchIdException extends RuntimeException {

  public NoSuchIdException(String message) {
    super(message);
  }

}
