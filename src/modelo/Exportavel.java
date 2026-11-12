package modelo;

/** Interface para objetos que sabem gerar a sua linha no arquivo CSV. */
public interface Exportavel {
    String toCsv();
}
