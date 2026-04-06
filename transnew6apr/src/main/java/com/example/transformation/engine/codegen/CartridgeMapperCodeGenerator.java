package com.example.transformation.engine.codegen;

import com.example.transformation.engine.MappingTemplate;
import com.example.transformation.engine.MappingTemplate.MappingRule;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.beans.Introspector;
import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Build-time code generator: reads JSON mapping templates and produces
 * type-safe Java mapper classes (Volante-style cartridge code generation).
 *
 * <p>Validates all source/target paths against actual DTO classes at build time;
 * the build fails immediately if any path is invalid.
 *
 * <p>Usage: {@code java CartridgeMapperCodeGenerator <templateDir> <outputDir>}
 */
public class CartridgeMapperCodeGenerator {

    static final String GEN_PKG = "com.example.transformation.engine.generated";
    static final String SRC_ROOT = "com.example.transformation.dto.canonical.CanonicalPayment";
    static final String TF = "com.example.transformation.engine.TransformFunctions";

    private static final Pattern ARR = Pattern.compile("(.+)\\[(\\d+)]");

    private static final Map<String, String> TX = Map.ofEntries(
        Map.entry("uppercase", "uppercase"), Map.entry("lowercase", "lowercase"),
        Map.entry("trim", "trim"), Map.entry("toString", "asString"),
        Map.entry("toMoney", "toMoney"), Map.entry("toLong", "toLong"),
        Map.entry("firstElement", "firstElement"),
        Map.entry("firstNonBlankPhone", "firstNonBlankPhone"),
        Map.entry("phoneNumberType", "phoneNumberType"),
        Map.entry("recipientType", "recipientType"));

    private final ObjectMapper om = new ObjectMapper();
    private final List<String> errors = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: CartridgeMapperCodeGenerator <templateDir> <outputDir>");
            System.exit(1);
        }
        var gen = new CartridgeMapperCodeGenerator();
        gen.run(Path.of(args[0]), Path.of(args[1]));
        if (!gen.errors.isEmpty()) {
            System.err.println("\n=== BUILD-TIME MAPPING VALIDATION ERRORS ===");
            gen.errors.forEach(e -> System.err.println("  ERROR: " + e));
            System.exit(1);
        }
        System.out.println("Cartridge mapper code generation complete.");
    }

    void run(Path templateDir, Path outputDir) throws Exception {
        if (!Files.isDirectory(templateDir)) {
            System.out.println("Template dir not found, skipping: " + templateDir);
            return;
        }
        File[] files = templateDir.toFile().listFiles((d, n) -> n.endsWith(".json"));
        if (files == null || files.length == 0) {
            System.out.println("No templates in: " + templateDir);
            return;
        }
        Path pkgDir = outputDir.resolve(GEN_PKG.replace('.', '/'));
        Files.createDirectories(pkgDir);
        for (File f : files) {
            String key = f.getName().replace(".json", "");
            MappingTemplate tpl = om.readValue(f, MappingTemplate.class);
            emit(tpl, key, pkgDir);
        }
    }

    // ── per-template class generation ────────────────────────────────────────

    private void emit(MappingTemplate tpl, String key, Path pkgDir) throws Exception {
        Class<?> srcRoot = Class.forName(SRC_ROOT);
        Class<?> tgtRoot;
        try { tgtRoot = Class.forName(tpl.getTargetClass()); }
        catch (ClassNotFoundException e) {
            errors.add("[" + key + "] Target class not found: " + tpl.getTargetClass());
            return;
        }

        String cls = toPascal(key) + "Mapper";
        String tgtSimple = tgtRoot.getSimpleName();

        Set<String> imports = new LinkedHashSet<>();
        imports.add(SRC_ROOT);
        imports.add(SRC_ROOT + ".CanonicalTransaction");
        imports.add(tpl.getTargetClass());
        imports.add(TF);
        imports.add("com.example.transformation.engine.CartridgeMapper");
        imports.add("org.springframework.stereotype.Component");

        // track nested target objects that need instantiation
        Map<String, NVar> nested = new LinkedHashMap<>();
        List<String> body = new ArrayList<>();

        body.add("        " + tgtSimple + " target = new " + tgtSimple + "();");
        body.add("        CanonicalTransaction txn = source.getTransaction();");
        body.add("        if (txn == null) return target;");

        for (MappingRule r : tpl.getRules()) {
            emitRule(r, key, srcRoot, tgtRoot, imports, nested, body);
        }

        // wire nested objects back into their parents
        body.add("");
        for (var e : nested.entrySet()) {
            body.add("        " + e.getValue().parentSetter + "(" + e.getKey() + ");");
        }
        body.add("        return target;");

        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(pkgDir.resolve(cls + ".java")))) {
            w.println("package " + GEN_PKG + ";");
            w.println();
            for (String imp : imports) w.println("import " + imp + ";");
            w.println();
            w.println("/**");
            w.println(" * Generated cartridge mapper for template: " + key);
            w.println(" *");
            w.println(" * <p>GENERATED CODE — DO NOT EDIT.");
            w.println(" * Modify the JSON template and re-run the build.");
            w.println(" */");
            w.println("@Component");
            w.println("public final class " + cls + " implements CartridgeMapper<" + tgtSimple + "> {");
            w.println();
            w.println("    @Override");
            w.println("    public String templateKey() { return \"" + key + "\"; }");
            w.println();
            w.println("    @Override");
            w.println("    public " + tgtSimple + " map(CanonicalPayment source) {");
            for (String line : body) w.println(line);
            w.println("    }");
            w.println("}");
        }
        System.out.println("  Generated: " + cls + " (" + tpl.getRules().size() + " rules)");
    }

    // ── single mapping rule ──────────────────────────────────────────────────

    private void emitRule(MappingRule rule, String key,
                          Class<?> srcRoot, Class<?> tgtRoot,
                          Set<String> imports, Map<String, NVar> nested,
                          List<String> body) {

        SChain chain = setterChain(rule.getTarget(), key, tgtRoot, imports, nested, body);
        if (chain == null) return;
        Class<?> pt = chain.paramType;

        boolean hasSrc = rule.getSource() != null && !rule.getSource().isBlank();
        boolean hasDef = rule.getDefaultValue() != null;
        boolean hasTx  = rule.getTransform() != null && !rule.getTransform().isBlank();

        // (A) default-only — no source, no transform
        if (!hasSrc && hasDef && !hasTx) {
            body.add("        " + chain.call + literal(rule.getDefaultValue(), pt, imports) + ");");
            return;
        }

        String srcExpr = hasSrc ? srcExpr(rule.getSource(), key, srcRoot) : null;
        if (hasSrc && srcExpr == null) return; // error already logged

        // (B) has transform
        if (hasTx) {
            String method = TX.get(rule.getTransform());
            if (method == null) { errors.add("[" + key + "] Unknown transform: " + rule.getTransform()); return; }

            String input = srcExpr != null ? "(Object) " + srcExpr : literal(rule.getDefaultValue(), String.class, imports);
            String txCall = "TransformFunctions." + method + "(" + input + ")";
            String cast = castObj(txCall, pt, imports);

            if (hasSrc && !hasDef) {
                body.add("        if (" + srcExpr + " != null) " + chain.call + cast + ");");
            } else {
                body.add("        " + chain.call + cast + ");");
            }
            return;
        }

        // (C) source-only — direct assignment
        Class<?> srcType = srcType(rule.getSource(), srcRoot);
        String coerced = coerce(srcExpr, srcType, pt, imports);
        if (!hasDef) {
            body.add("        if (" + srcExpr + " != null) " + chain.call + coerced + ");");
        } else {
            String lit = literal(rule.getDefaultValue(), pt, imports);
            body.add("        " + chain.call + "(" + srcExpr + " != null ? " + coerced + " : " + lit + "));");
        }
    }

    // ── source path → getter expression ──────────────────────────────────────

    private String srcExpr(String path, String key, Class<?> srcRoot) {
        String[] segs = path.split("\\.");
        if ("transaction".equals(segs[0])) {
            StringBuilder e = new StringBuilder("txn");
            Class<?> c;
            try { c = srcRoot.getMethod("getTransaction").getReturnType(); }
            catch (NoSuchMethodException ex) { errors.add("[" + key + "] No getTransaction()"); return null; }
            for (int i = 1; i < segs.length; i++) {
                Method g = getter(c, segs[i]);
                if (g == null) { errors.add("[" + key + "] Source '" + path + "': no getter '" + segs[i] + "' on " + c.getSimpleName()); return null; }
                e.append(".").append(g.getName()).append("()");
                c = g.getReturnType();
            }
            return e.toString();
        }
        StringBuilder e = new StringBuilder("source");
        Class<?> c = srcRoot;
        for (String seg : segs) {
            Method g = getter(c, seg);
            if (g == null) { errors.add("[" + key + "] Source '" + path + "': no getter '" + seg + "' on " + c.getSimpleName()); return null; }
            e.append(".").append(g.getName()).append("()");
            c = g.getReturnType();
        }
        return e.toString();
    }

    private Class<?> srcType(String path, Class<?> srcRoot) {
        String[] segs = path.split("\\.");
        Class<?> c;
        if ("transaction".equals(segs[0])) {
            try { c = srcRoot.getMethod("getTransaction").getReturnType(); }
            catch (NoSuchMethodException ex) { return Object.class; }
            for (int i = 1; i < segs.length; i++) {
                Method g = getter(c, segs[i]);
                if (g == null) return Object.class;
                c = g.getReturnType();
            }
            return c;
        }
        c = srcRoot;
        for (String seg : segs) {
            Method g = getter(c, seg);
            if (g == null) return Object.class;
            c = g.getReturnType();
        }
        return c;
    }

    // ── target path → setter chain ───────────────────────────────────────────

    private SChain setterChain(String path, String key, Class<?> tgtRoot,
                                Set<String> imports, Map<String, NVar> nested,
                                List<String> body) {
        String[] segs = path.split("\\.");
        String obj = "target";
        Class<?> cls = tgtRoot;

        for (int i = 0; i < segs.length - 1; i++) {
            String seg = noArr(segs[i]);
            Method g = getter(cls, seg);
            if (g == null) { errors.add("[" + key + "] Target '" + path + "': no getter '" + seg + "' on " + cls.getSimpleName()); return null; }
            Class<?> ft = g.getReturnType();
            String var = varName(segs, i);
            if (!nested.containsKey(var)) {
                addImport(imports, ft);
                nested.put(var, new NVar(ft, obj + ".set" + cap(seg)));
                body.add("");
                body.add("        " + typeRef(ft) + " " + var + " = new " + typeRef(ft) + "();");
            }
            obj = var;
            cls = ft;
        }

        String last = noArr(segs[segs.length - 1]);
        Method s = setter(cls, last);
        if (s == null) { errors.add("[" + key + "] Target '" + path + "': no setter '" + last + "' on " + cls.getSimpleName()); return null; }
        return new SChain(obj + "." + s.getName() + "(", s.getParameterTypes()[0]);
    }

    // ── type coercion ────────────────────────────────────────────────────────

    private String literal(String val, Class<?> t, Set<String> imports) {
        if (t.isEnum()) { addImport(imports, t); return typeRef(t) + ".fromValue(\"" + esc(val) + "\")"; }
        if (t == Long.class || t == long.class) return val + "L";
        if (t == Double.class || t == double.class) return val + "d";
        if (t == Integer.class || t == int.class) return val;
        return "\"" + esc(val) + "\"";
    }

    private String coerce(String expr, Class<?> from, Class<?> to, Set<String> imports) {
        if (to.isAssignableFrom(from)) return expr;
        if (to == String.class) return "String.valueOf(" + expr + ")";
        if ((to == Double.class || to == double.class) && Number.class.isAssignableFrom(box(from)))
            return expr + ".doubleValue()";
        if ((to == Long.class || to == long.class) && Number.class.isAssignableFrom(box(from)))
            return expr + ".longValue()";
        if (to.isEnum()) { addImport(imports, to); return typeRef(to) + ".fromValue(String.valueOf(" + expr + "))"; }
        return expr;
    }

    private String castObj(String expr, Class<?> to, Set<String> imports) {
        if (to == String.class) return "(String) " + expr;
        if (to == Double.class || to == double.class) return "((Number) " + expr + ").doubleValue()";
        if (to == Long.class || to == long.class) return "((Number) " + expr + ").longValue()";
        if (to.isEnum()) { addImport(imports, to); return typeRef(to) + ".fromValue(String.valueOf(" + expr + "))"; }
        if (to == Object.class) return expr;
        return "(" + typeRef(to) + ") " + expr;
    }

    private Class<?> box(Class<?> c) {
        if (c == int.class) return Integer.class;
        if (c == long.class) return Long.class;
        if (c == double.class) return Double.class;
        if (c == float.class) return Float.class;
        return c;
    }

    // ── reflection ───────────────────────────────────────────────────────────

    private Method getter(Class<?> c, String f) {
        String cap = cap(f);
        try { return c.getMethod("get" + cap); } catch (NoSuchMethodException ignored) {}
        try { return c.getMethod("is" + cap); } catch (NoSuchMethodException ignored) {}
        return null;
    }

    private Method setter(Class<?> c, String f) {
        String n = "set" + cap(f);
        for (Method m : c.getMethods()) if (m.getName().equals(n) && m.getParameterCount() == 1) return m;
        return null;
    }

    // ── string utilities ─────────────────────────────────────────────────────

    private String varName(String[] segs, int upTo) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= upTo; i++) {
            String s = noArr(segs[i]);
            sb.append(i == 0 ? Introspector.decapitalize(s) : cap(s));
        }
        return sb.toString();
    }

    private String noArr(String s) { Matcher m = ARR.matcher(s); return m.matches() ? m.group(1) : s; }

    /**
     * Returns the type reference to use in generated code.
     * For top-level classes: simple name (e.g. "PayoutMethod").
     * For inner classes: "EnclosingClass.InnerClass" (e.g. "AccountVerificationRequest.PayoutMethodEnum").
     */
    private String typeRef(Class<?> c) {
        if (c.getEnclosingClass() != null) {
            return c.getEnclosingClass().getSimpleName() + "." + c.getSimpleName();
        }
        return c.getSimpleName();
    }

    private void addImport(Set<String> imp, Class<?> c) {
        if (c.isPrimitive() || c.getPackageName().startsWith("java.lang")) return;
        if (c.getEnclosingClass() != null) {
            imp.add(c.getEnclosingClass().getName());
        } else {
            imp.add(c.getName());
        }
    }
    static String cap(String s) { return s == null || s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1); }
    static String toPascal(String s) {
        StringBuilder sb = new StringBuilder();
        for (String p : s.split("[_\\-]")) if (!p.isEmpty()) { sb.append(Character.toUpperCase(p.charAt(0))); sb.append(p.substring(1).toLowerCase()); }
        return sb.toString();
    }
    static String esc(String s) { return s.replace("\\", "\\\\").replace("\"", "\\\""); }

    record NVar(Class<?> type, String parentSetter) {}
    record SChain(String call, Class<?> paramType) {}
}
