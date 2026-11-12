package excecao;

/** Lançada quando um usuário sem permissão (perfil CONSULTA) tenta alterar dados. */
public class AcessoNegadoException extends SistemaException {
    public AcessoNegadoException() {
        super("Seu perfil (CONSULTA) não permite alterar dados.");
    }
}
