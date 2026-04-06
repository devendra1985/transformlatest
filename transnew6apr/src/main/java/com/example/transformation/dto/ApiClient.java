package com.example.transformation.dto;

public final class ApiClient {
  private ApiClient() {}

  public static String valueToString(Object value) {
    return value == null ? "" : String.valueOf(value);
  }
}
