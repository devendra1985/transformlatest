package com.example.transformation.dto.visa;

public enum Type {
  C("C"),
  I("I");

  private final String value;

  Type(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static Type fromValue(String value) {
    for (Type v : values()) {
      if (v.value.equals(value)) {
        return v;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}
