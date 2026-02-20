package com.example.transformation.processor;

import com.example.transformation.cartridge.CartridgeException;
import com.example.transformation.cartridge.ErrorCodes;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component("externalApiCall")
public class ExternalApiProcessor implements Processor {
  private static final Logger LOG = LoggerFactory.getLogger(ExternalApiProcessor.class);
  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  private final String baseUrl;
  private final String path;
  private final boolean enabled;

  public ExternalApiProcessor(
      ObjectMapper objectMapper,
      @Value("${app.visa.base-url}") String baseUrl,
      @Value("${app.visa.path}") String path,
      @Value("${app.visa.invoke-enabled:true}") boolean enabled) {
    this.restTemplate = new RestTemplate();
    this.objectMapper = objectMapper;
    this.baseUrl = baseUrl;
    this.path = path;
    this.enabled = enabled;
  }

  @Override
  public void process(Exchange exchange) {
    Object body = exchange.getMessage().getBody();
    if (!enabled) {
      String payload = toJson(body);
      LOG.info("External API disabled. Returning transformed DTO.");
      exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/json");
      exchange.getMessage().setBody(payload);
      return;
    }
    String url = baseUrl + path;
    try {
      ResponseEntity<String> response = restTemplate.exchange(
          url,
          HttpMethod.POST,
          new HttpEntity<>(body),
          String.class
      );
      exchange.getMessage().setBody(response.getBody());
      if (response.getHeaders().getContentType() != null) {
        exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, response.getHeaders().getContentType().toString());
      }
    } catch (RestClientException ex) {
      throw new CartridgeException(
          ErrorCodes.code(ErrorCodes.GENERIC_TECHNICAL),
          CartridgeException.ErrorType.TECHNICAL,
          "External API call failed: " + ex.getMessage(),
          ex,
          null,
          "EXTERNAL");
    }
  }

  private String toJson(Object body) {
    if (body == null) {
      return "null";
    }
    try {
      return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body);
    } catch (JsonProcessingException ex) {
      return String.valueOf(body);
    }
  }
}
