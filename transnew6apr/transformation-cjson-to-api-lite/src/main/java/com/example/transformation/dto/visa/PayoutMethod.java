package com.example.transformation.dto.visa;

public enum PayoutMethod {
  B("B");

  private final String value;

  PayoutMethod(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static PayoutMethod fromValue(String value) {
    for (PayoutMethod v : values()) {
      if (v.value.equals(value)) {
        return v;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}
