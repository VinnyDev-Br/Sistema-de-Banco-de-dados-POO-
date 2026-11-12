package util;

import java.text.Normalizer;

public final class Texto {
    private Texto() { }

    /** Minúsculas e sem acentos, para buscas que ignoram "João" x "joao". */
    public static String normalizar(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase().trim();
    }
}
