package util;

import java.util.ArrayList;
import java.util.List;

/** Utilitário para montar e quebrar linhas CSV separadas por ';' (padrão do Excel em português). */
public final class Csv {
    private Csv() { }

    /** Escapa '\', ';' e quebras de linha para o texto caber dentro de uma coluna. */
    public static String escapar(Object valor) {
        if (valor == null) return "";
        String s = String.valueOf(valor);
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case ';':  sb.append("\\;");  break;
                case '\n': sb.append("\\n");  break;
                case '\r': break;
                default:   sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String juntar(Object... campos) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) sb.append(';');
            sb.append(escapar(campos[i]));
        }
        return sb.toString();
    }

    /** Operação inversa de juntar(): respeita as barras de escape. */
    public static List<String> dividir(String linha) {
        List<String> campos = new ArrayList<>();
        StringBuilder atual = new StringBuilder();
        for (int i = 0; i < linha.length(); i++) {
            char c = linha.charAt(i);
            if (c == '\\' && i + 1 < linha.length()) {
                char prox = linha.charAt(++i);
                atual.append(prox == 'n' ? '\n' : prox);
            } else if (c == ';') {
                campos.add(atual.toString());
                atual.setLength(0);
            } else {
                atual.append(c);
            }
        }
        campos.add(atual.toString());
        return campos;
    }
}
