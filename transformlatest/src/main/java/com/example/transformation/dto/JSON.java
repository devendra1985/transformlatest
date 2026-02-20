package com.example.transformation.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Set;

public final class JSON {
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final ConcurrentMap<Class<?>, Map<String, Class<?>>> DESCENDANTS = new ConcurrentHashMap<>();

  private JSON() {}

  public static ObjectMapper getMapper() {
    return MAPPER;
  }

  public static void registerDescendants(Class<?> parent, Map<String, Class<?>> descendants) {
    if (parent != null && descendants != null) {
      DESCENDANTS.put(parent, descendants);
    }
  }

  public static boolean isInstanceOf(Class<?> clazz, Object instance, Set<Class<?>> visited) {
    if (clazz == null || instance == null) {
      return false;
    }
    return clazz.isInstance(instance);
  }
}
