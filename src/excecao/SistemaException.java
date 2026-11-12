package excecao;

/** Exceção base do sistema (herda de Exception, ou seja, é verificada). */
public class SistemaException extends Exception {
    public SistemaException(String mensagem) { super(mensagem); }
    public SistemaException(String mensagem, Throwable causa) { super(mensagem, causa); }
}
