package com.example.transformation.enrich;

import java.util.ArrayList;
import java.util.List;

/**
 * YAML model for per-cartridge enrichment rules.
 */
public class EnrichmentConfig {
  public String cartridgeId;
  public List<Rule> rules = new ArrayList<>();

  public static class Rule {
    public When when;
    public Set set;
    public Copy copy;
    public Call call;
  }

  public static class When {
    public String path;
    public String equals;
    public Boolean exists;
  }

  public static class Set {
    public String target;
    public Object value;
  }

  public static class Copy {
    public String source;
    public String target;
  }

  public static class Call {
    public String bean;
    public String method;
    public String target;
  }
}
