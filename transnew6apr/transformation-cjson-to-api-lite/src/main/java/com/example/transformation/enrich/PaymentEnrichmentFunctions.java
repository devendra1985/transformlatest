package com.example.transformation.enrich;

import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Payment-specific enrichment functions.
 */
public class PaymentEnrichmentFunctions {
    
    private static final Logger log = LoggerFactory.getLogger(PaymentEnrichmentFunctions.class);
    
    private static final double HIGH_VALUE_THRESHOLD = 10000.0;
    private static final double VERY_HIGH_VALUE_THRESHOLD = 50000.0;
    
    public Map<String, Object> calculateRiskScore(Map<String, Object> body) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        int riskScore = 0;
        
        Double amount = extractAmount(body);
        if (amount != null) {
            if (amount >= VERY_HIGH_VALUE_THRESHOLD) {
                riskScore += 40;
            } else if (amount >= HIGH_VALUE_THRESHOLD) {
                riskScore += 25;
            } else if (amount >= 5000) {
                riskScore += 10;
            }
        }
        
        String paymentType = extractString(body, "paymentType");
        if ("WIRE".equalsIgnoreCase(paymentType)) {
            riskScore += 20;
        } else if ("INSTANT".equalsIgnoreCase(paymentType)) {
            riskScore += 15;
        } else if ("ACH".equalsIgnoreCase(paymentType)) {
            riskScore += 5;
        }
        
        Map<?, ?> payee = extractMap(body, "payee");
        if (payee != null) {
            String bankCode = (String) payee.get("bankCode");
            if (bankCode != null && bankCode.length() == 11) {
                riskScore += 15;
            }
        }
        
        Boolean newPayee = extractBoolean(body, "newPayee");
        if (Boolean.TRUE.equals(newPayee)) {
            riskScore += 10;
        }
        
        riskScore = Math.min(riskScore, 100);
        
        String riskLevel;
        if (riskScore >= 70) {
            riskLevel = "CRITICAL";
        } else if (riskScore >= 50) {
            riskLevel = "HIGH";
        } else if (riskScore >= 25) {
            riskLevel = "MEDIUM";
        } else {
            riskLevel = "LOW";
        }
        
        result.put("riskScore", riskScore);
        result.put("riskLevel", riskLevel);
        
        log.debug("Calculated risk score: {} ({})", riskScore, riskLevel);
        
        return result;
    }
    
    public Map<String, Object> enrichPayerDetails(Map<String, Object> body) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        Map<?, ?> payer = extractMap(body, "payer");
        if (payer == null) {
            return result;
        }
        
        String customerId = (String) payer.get("customerId");
        if (customerId == null || customerId.isBlank()) {
            return result;
        }
        
        Map<String, Object> payerEnrichment = new LinkedHashMap<>();
        payerEnrichment.put("customerTier", "PREMIUM");
        payerEnrichment.put("kycVerified", true);
        payerEnrichment.put("accountAge", "5 years");
        
        result.put("payerDetails", payerEnrichment);
        
        log.debug("Enriched payer details for customerId: {}", customerId);
        
        return result;
    }
    
    public Map<String, Object> normalizeIban(Map<String, Object> body) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        Map<?, ?> payee = extractMap(body, "payee");
        if (payee == null) {
            return result;
        }
        
        String accountNumber = (String) payee.get("accountNumber");
        if (accountNumber != null) {
            String normalized = accountNumber.replaceAll("\\s+", "").toUpperCase();
            result.put("payee.normalizedAccount", normalized);
        }
        
        return result;
    }
    
    private Double extractAmount(Map<String, Object> body) {
        Object amount = body.get("amount");
        if (amount instanceof Number n) {
            return n.doubleValue();
        }
        if (amount instanceof String s) {
            try {
                return Double.parseDouble(s.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    private String extractString(Map<String, Object> body, String key) {
        Object value = body.get(key);
        return value instanceof String s ? s : null;
    }
    
    private Boolean extractBoolean(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof String s) {
            return "true".equalsIgnoreCase(s);
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractMap(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value instanceof Map<?, ?>) {
            return (Map<String, Object>) value;
        }
        return null;
    }
}
