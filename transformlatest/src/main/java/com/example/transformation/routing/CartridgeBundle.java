package com.example.transformation.routing;

import lombok.Data;

@Data
public class CartridgeBundle {
  private String cartridgeId;
  private String templateId;
  private String enrichPath;
  private String rulesPath;
  private String routePath;
}
