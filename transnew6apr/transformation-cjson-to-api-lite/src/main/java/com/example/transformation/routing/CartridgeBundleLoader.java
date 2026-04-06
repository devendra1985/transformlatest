package com.example.transformation.routing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.Map;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class CartridgeBundleLoader {
  private final ObjectMapper objectMapper;
  private final ResourceLoader resourceLoader;
  private volatile Map<String, CartridgeBundle> cache;

  public CartridgeBundleLoader(ObjectMapper objectMapper, ResourceLoader resourceLoader) {
    this.objectMapper = objectMapper;
    this.resourceLoader = resourceLoader;
  }

  public CartridgeBundle getBundle(String key) {
    Map<String, CartridgeBundle> bundles = load();
    return bundles.get(key);
  }

  public Map<String, CartridgeBundle> getAllBundles() {
    return load();
  }

  private Map<String, CartridgeBundle> load() {
    if (cache != null) {
      return cache;
    }
    synchronized (this) {
      if (cache != null) {
        return cache;
      }
      try {
        Resource resource = resourceLoader.getResource("classpath:cartridge.json");
        try (InputStream input = resource.getInputStream()) {
          cache = objectMapper.readValue(input, new TypeReference<>() {});
          return cache;
        }
      } catch (Exception e) {
        throw new IllegalStateException("Failed to load cartridge.json", e);
      }
    }
  }
}
