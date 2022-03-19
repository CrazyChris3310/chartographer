package com.danil.chartographer.exception;

/**
 * {@code SizeException} is thrown when sizes of the image or it's coordinates are invalid
 */
public class SizeException extends RuntimeException {

  public SizeException(String message) {
    super(message);
  }

}
