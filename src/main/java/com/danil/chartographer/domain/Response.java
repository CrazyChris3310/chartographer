package com.danil.chartographer.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
/**
 * Response is used to be returned by server, containing a message
 */
@Data
@AllArgsConstructor
public class Response {

  private String message;

}
