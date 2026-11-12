package excecao;

/** Lançada quando um dado digitado não passa na validação (campo vazio, valor negativo, data inválida...). */
public class DadoInvalidoException extends SistemaException {
    public DadoInvalidoException(String mensagem) { super(mensagem); }
}
