package com.matcher.platform.security;

import java.util.regex.Pattern;

public final class XssSanitizer {

    private static final Pattern SCRIPT_PATTERN = Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE);
    private static final Pattern SRC_PATTERN = Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern SCRIPT_TAG_PATTERN = Pattern.compile("</script>", Pattern.CASE_INSENSITIVE);
    private static final Pattern SCRIPT_OPEN_PATTERN = Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern EVAL_PATTERN = Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern JAVASCRIPT_PATTERN = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
    private static final Pattern VBSCRIPT_PATTERN = Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE);
    private static final Pattern ONLOAD_PATTERN = Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern ONERROR_PATTERN = Pattern.compile("onerror(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern ONCLICK_PATTERN = Pattern.compile("onclick(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern ONMOUSE_PATTERN = Pattern.compile("onmouse(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    private XssSanitizer() {
    }

    public static String sanitize(String value) {
        if (value == null) {
            return null;
        }

        String clean = value;
        clean = SCRIPT_PATTERN.matcher(clean).replaceAll("");
        clean = SCRIPT_TAG_PATTERN.matcher(clean).replaceAll("");
        clean = SCRIPT_OPEN_PATTERN.matcher(clean).replaceAll("");
        clean = EVAL_PATTERN.matcher(clean).replaceAll("");
        clean = EXPRESSION_PATTERN.matcher(clean).replaceAll("");
        clean = JAVASCRIPT_PATTERN.matcher(clean).replaceAll("");
        clean = VBSCRIPT_PATTERN.matcher(clean).replaceAll("");
        clean = ONLOAD_PATTERN.matcher(clean).replaceAll("");
        clean = ONERROR_PATTERN.matcher(clean).replaceAll("");
        clean = ONCLICK_PATTERN.matcher(clean).replaceAll("");
        clean = ONMOUSE_PATTERN.matcher(clean).replaceAll("");

        // Strip HTML tag angle brackets for safe storage
        clean = clean.replace("<", "&lt;").replace(">", "&gt;");

        return clean.trim();
    }
}
