package excecao;

/** Lançada ao tentar cadastrar um NIP que já existe. */
public class MatriculaDuplicadaException extends SistemaException {
    public MatriculaDuplicadaException(int nip) {
        super("Já existe uma pessoa cadastrada com o NIP " + nip + ".");
    }
}
