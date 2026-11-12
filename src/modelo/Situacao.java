package modelo;

public enum Situacao {
    ATIVO("Ativo"), AFASTADO("Afastado"), DESLIGADO("Desligado");

    private final String rotulo;
    Situacao(String rotulo) { this.rotulo = rotulo; }

    @Override public String toString() { return rotulo; }

    /** Converte texto (nome do enum ou rótulo) em Situacao, ignorando maiúsculas. */
    public static Situacao de(String texto) {
        if (texto != null) {
            for (Situacao s : values()) {
                if (s.name().equalsIgnoreCase(texto.trim()) || s.rotulo.equalsIgnoreCase(texto.trim())) return s;
            }
        }
        throw new IllegalArgumentException("Situação desconhecida: " + texto);
    }
}
