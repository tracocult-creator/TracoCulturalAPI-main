package com.TracoCultural.TracoCultural.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

 
public class TextUtils {

    private static final Pattern DIACRITICOS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private TextUtils() {}

    public static String normalize(String value) {
        if (value == null) return "";
        String semAcento = Normalizer.normalize(value, Normalizer.Form.NFD);
        semAcento = DIACRITICOS.matcher(semAcento).replaceAll("");
        return semAcento.toLowerCase().trim();
    }

    public static boolean contains(String haystack, String needle) {
        if (needle == null || needle.isBlank()) return true;
        return normalize(haystack).contains(normalize(needle));
    }
}
