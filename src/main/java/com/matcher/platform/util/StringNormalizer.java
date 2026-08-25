package com.matcher.platform.util;

import java.util.Locale;

public final class StringNormalizer {

    private static final double DEFAULT_FUZZY_THRESHOLD = 0.78;

    private StringNormalizer() {
    }

    /**
     * Trims leading/trailing whitespace and collapses multiple internal whitespaces into a single space.
     */
    public static String normalize(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("\\s+", " ").trim();
    }

    /**
     * Converts string to a canonical form (lowercased, spaces/hyphens/dots removed, '&' normalized to 'and').
     * Example: "Spring-Boot" -> "springboot", "React.js" -> "reactjs", "Data Structures & Algorithms" -> "datastructuresandalgorithms"
     */
    public static String canonicalize(String input) {
        if (input == null) {
            return "";
        }
        String s = input.toLowerCase(Locale.ROOT).trim();
        s = s.replace("&", "and");
        // Keep alphanumeric, '+', '#' (for C++, C#)
        return s.replaceAll("[^a-z0-9+#]", "");
    }

    /**
     * Checks whether two strings match via exact ignore-case, canonical match, or fuzzy similarity above default threshold (0.78).
     */
    public static boolean isFuzzyMatch(String strA, String strB) {
        return isFuzzyMatch(strA, strB, DEFAULT_FUZZY_THRESHOLD);
    }

    /**
     * Checks whether two strings match via exact ignore-case, canonical match, or fuzzy similarity above given threshold.
     */
    public static boolean isFuzzyMatch(String strA, String strB, double threshold) {
        if (strA == null || strB == null) {
            return false;
        }

        String normA = normalize(strA);
        String normB = normalize(strB);

        if (normA.equalsIgnoreCase(normB)) {
            return true;
        }

        String canonA = canonicalize(normA);
        String canonB = canonicalize(normB);

        if (!canonA.isEmpty() && canonA.equals(canonB)) {
            return true;
        }

        // Fuzzy similarity on normalized strings
        double simNorm = calculateSimilarity(normA.toLowerCase(Locale.ROOT), normB.toLowerCase(Locale.ROOT));
        if (simNorm >= threshold) {
            return true;
        }

        // Fuzzy similarity on canonical strings
        if (!canonA.isEmpty() && !canonB.isEmpty()) {
            double simCanon = calculateSimilarity(canonA, canonB);
            return simCanon >= threshold;
        }

        return false;
    }

    /**
     * Calculates normalized Damerau-Levenshtein similarity score between 0.0 and 1.0.
     */
    public static double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }
        if (s1.equals(s2)) {
            return 1.0;
        }
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) {
            return 1.0;
        }
        int distance = computeDamerauLevenshteinDistance(s1, s2);
        return 1.0 - ((double) distance / maxLen);
    }

    private static int computeDamerauLevenshteinDistance(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();
        int[][] d = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            d[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            d[0][j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            char c1 = s1.charAt(i - 1);
            for (int j = 1; j <= len2; j++) {
                char c2 = s2.charAt(j - 1);
                int cost = (c1 == c2) ? 0 : 1;

                d[i][j] = Math.min(
                        Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1),
                        d[i - 1][j - 1] + cost
                );

                // Damerau adjacent character transposition check
                if (i > 1 && j > 1 && c1 == s2.charAt(j - 2) && s1.charAt(i - 2) == c2) {
                    d[i][j] = Math.min(d[i][j], d[i - 2][j - 2] + 1);
                }
            }
        }
        return d[len1][len2];
    }
}
