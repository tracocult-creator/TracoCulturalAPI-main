package com.TracoCultural.TracoCultural.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Remove acentos e normaliza caixa para comparações de texto.
 * Espelha a lógica de lib/text.ts do app mobile, para que busca e
 * filtros se comportem da mesma forma em todas as pontas.
 */
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
