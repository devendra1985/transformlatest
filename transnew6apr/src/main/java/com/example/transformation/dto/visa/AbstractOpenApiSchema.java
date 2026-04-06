package com.example.transformation.dto.visa;

import java.util.Map;

public abstract class AbstractOpenApiSchema {
  private final String schemaType;
  private final Boolean nullable;
  private Object actualInstance;

  protected AbstractOpenApiSchema() {
    this(null, null);
  }

  protected AbstractOpenApiSchema(String schemaType, Boolean nullable) {
    this.schemaType = schemaType;
    this.nullable = nullable;
  }

  public Object getActualInstance() {
    return actualInstance;
  }

  public void setActualInstance(Object instance) {
    this.actualInstance = instance;
  }

  public String getSchemaType() {
    return schemaType;
  }

  public Boolean getNullable() {
    return nullable;
  }

  public Map<String, Class<?>> getSchemas() {
    return null;
  }
}
