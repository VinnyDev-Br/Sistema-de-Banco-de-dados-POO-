package servico;

import excecao.ArmazenamentoException;
import excecao.DadoInvalidoException;
import modelo.Usuario;
import repositorio.ArquivoUsuarios;
import util.Senha;

import java.nio.file.Path;
import java.util.List;

public class ServicoAutenticacao {
    private final List<Usuario> usuarios;

    public ServicoAutenticacao(Path pasta) throws ArmazenamentoException {
        this.usuarios = new ArquivoUsuarios(pasta).carregar();
    }

    public Usuario autenticar(String login, char[] senha) throws DadoInvalidoException {
        String hash = Senha.hash(new String(senha));
        for (Usuario u : usuarios) {
            if (u.getLogin().equalsIgnoreCase(login == null ? "" : login.trim()) && u.getSenhaHash().equals(hash)) return u;
        }
        throw new DadoInvalidoException("Login ou senha incorretos.");
    }
}
