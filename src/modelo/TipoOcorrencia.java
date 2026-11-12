package modelo;

public enum TipoOcorrencia {
    NEGATIVA("Negativa"), ELOGIO("Elogio"), PARTE_OCORRENCIA("Parte de ocorrência");

    private final String rotulo;
    TipoOcorrencia(String rotulo) { this.rotulo = rotulo; }

    @Override public String toString() { return rotulo; }
}
