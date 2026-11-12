package excecao;

/** Lançada quando há falha ao ler ou gravar arquivos (encapsula IOException). */
public class ArmazenamentoException extends SistemaException {
    public ArmazenamentoException(String mensagem, Throwable causa) { super(mensagem, causa); }
}
