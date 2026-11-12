package excecao;

/** Lançada quando uma busca, edição ou remoção não encontra o NIP informado. */
public class PessoaNaoEncontradaException extends SistemaException {
    public PessoaNaoEncontradaException(int nip) {
        super("Nenhuma pessoa encontrada com o NIP " + nip + ".");
    }
}
