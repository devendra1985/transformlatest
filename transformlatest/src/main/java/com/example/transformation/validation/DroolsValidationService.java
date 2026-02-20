package com.example.transformation.validation;

import com.example.transformation.cartridge.CartridgeException;
import com.example.transformation.cartridge.ErrorCodes;
import com.example.transformation.config.model.ResolvedCartridgeContext;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.kie.api.KieBase;
import org.kie.internal.utils.KieHelper;
import org.kie.api.io.ResourceType;
import org.kie.internal.io.ResourceFactory;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

@Service
public class DroolsValidationService {
  private static final Logger LOG = LoggerFactory.getLogger(DroolsValidationService.class);
  private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
  private final ConcurrentMap<String, KieBase> baseCache = new ConcurrentHashMap<>();

  public DroolsValidationService() {
  }

  public void validate(Object apiRequest, ResolvedCartridgeContext context) {
    List<ValidationError> errors = new ArrayList<>();
    KieSession session = getKieBase(context).newKieSession();
    try {
      session.setGlobal("errors", errors);
      session.insert(apiRequest);
      session.fireAllRules();
    } finally {
      session.dispose();
    }

    if (!errors.isEmpty()) {
      ValidationError first = errors.get(0);
      String message = buildMessage(errors);
      throw new CartridgeException(
          ErrorCodes.code(ErrorCodes.VALIDATION_REQUIRED),
          CartridgeException.ErrorType.FUNCTIONAL,
          message,
          first.getField(),
          "VALIDATION");
    }
  }

  public void preload(ResolvedCartridgeContext context) {
    getKieBase(context);
  }

  private KieBase getKieBase(ResolvedCartridgeContext context) {
    String key = context == null
        ? "default"
        : context.cartridgeId()
            + "|" + (context.currency() == null ? "" : context.currency())
            + "|" + (context.templateId() == null ? "" : context.templateId());
    return baseCache.computeIfAbsent(key, k -> buildKieBase(context));
  }

  private KieBase buildKieBase(ResolvedCartridgeContext context) {
    KieHelper helper = new KieHelper();
    if (context != null
        && context.provider() != null
        && context.cartridgeId() != null
        && context.templateId() != null
        && !context.templateId().isBlank()) {
      String templatePath = String.format(
          "cartridges/%s/%s/templates/%s/rules/*.drl",
          context.provider(),
          context.cartridgeId(),
          context.templateId());
      addClasspathPattern(helper, templatePath);
    }

    return helper.build();
  }

  private void addClasspathPattern(KieHelper helper, String pattern) {
    try {
      Resource[] resources = resolver.getResources("classpath*:" + pattern);
      for (Resource resource : resources) {
        try (var stream = resource.getInputStream()) {
          byte[] bytes = stream.readAllBytes();
          var drlResource = ResourceFactory.newByteArrayResource(bytes);
          String sourcePath = toClassPath(resource);
          drlResource.setSourcePath(sourcePath);
          helper.addResource(drlResource, ResourceType.DRL);
          if (LOG.isDebugEnabled()) {
            LOG.debug("Loaded Drools rule: {}", sourcePath);
          }
        }
      }
    } catch (Exception e) {
      throw new CartridgeException(
          ErrorCodes.code(ErrorCodes.VALIDATION_REQUIRED),
          CartridgeException.ErrorType.TECHNICAL,
          "Failed to load validation rules: " + pattern,
          null,
          "VALIDATION");
    }
  }

  private String buildMessage(List<ValidationError> errors) {
    StringBuilder sb = new StringBuilder("Validation failed: ");
    for (int i = 0; i < errors.size(); i++) {
      ValidationError error = errors.get(i);
      if (i > 0) {
        sb.append("; ");
      }
      sb.append(error.getField()).append(" - ").append(error.getMessage());
    }
    return sb.toString();
  }

  private String toClassPath(Resource resource) {
    try {
      String path = resource.getURL().getPath();
      int idx = path.indexOf("/classes/");
      if (idx >= 0) {
        return path.substring(idx + "/classes/".length());
      }
      return resource.getFilename() == null ? path : resource.getFilename();
    } catch (Exception e) {
      return resource.getFilename() == null ? "unknown.drl" : resource.getFilename();
    }
  }
}
