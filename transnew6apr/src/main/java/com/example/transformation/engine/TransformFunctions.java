package com.example.transformation.engine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Centralized business-logic transforms called directly from generated mapper code.
 *
 * <p>This is where business logic lives — NOT in the JSON template.
 * The template only references function names; the code generator emits
 * direct static calls to methods in this class.
 *
 * <p>To add a new transform: add a public static method here and use its name
 * in the mapping template JSON {@code "transform"} field.
 */
public final class TransformFunctions {

    private TransformFunctions() {}

    public static Object uppercase(Object v) {
        return v instanceof String s ? s.toUpperCase() : v;
    }

    public static Object lowercase(Object v) {
        return v instanceof String s ? s.toLowerCase() : v;
    }

    public static Object trim(Object v) {
        return v instanceof String s ? s.trim() : v;
    }

    public static Object asString(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    public static Object toMoney(Object v) {
        if (v instanceof BigDecimal bd) {
            return bd.setScale(2, RoundingMode.HALF_UP).doubleValue();
        }
        if (v instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue()).setScale(2, RoundingMode.HALF_UP).doubleValue();
        }
        return v;
    }

    public static Object toLong(Object v) {
        if (v instanceof Number n) return n.longValue();
        if (v instanceof String s) {
            try { return Long.parseLong(s.trim()); } catch (NumberFormatException e) { return null; }
        }
        return v;
    }

    public static Object firstElement(Object v) {
        if (v instanceof List<?> list && !list.isEmpty()) {
            return list.get(0);
        }
        return v;
    }

    /**
     * Maps recipient type codes: "B" -> "C" (company), null/blank -> "I" (individual).
     */
    public static Object recipientType(Object v) {
        if (v == null) return "I";
        String s = String.valueOf(v).trim();
        if (s.isBlank()) return "I";
        if ("B".equalsIgnoreCase(s)) return "C";
        return s;
    }

    public static Object firstNonBlankPhone(Object v) {
        return (v instanceof String s && !s.isBlank()) ? s : null;
    }

    public static Object phoneNumberType(Object v) {
        if (v == null) return null;
        String s = String.valueOf(v).trim();
        if (s.isBlank()) return null;
        return "HOME";
    }
}
