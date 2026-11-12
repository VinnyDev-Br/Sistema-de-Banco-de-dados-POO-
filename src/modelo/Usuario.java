package modelo;

import excecao.DadoInvalidoException;

/** Usuário do sistema. A senha é guardada apenas como hash SHA-256. */
public class Usuario {
    private String login;
    private String senhaHash;
    private Perfil perfil;

    public Usuario() { }

    public Usuario(String login, String senhaHash, Perfil perfil) throws DadoInvalidoException {
        setLogin(login); setSenhaHash(senhaHash); setPerfil(perfil);
    }

    public String getLogin() { return login; }
    public void setLogin(String login) throws DadoInvalidoException {
        if (login == null || login.trim().isEmpty()) throw new DadoInvalidoException("O login não pode ficar vazio.");
        this.login = login.trim();
    }
    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String h) throws DadoInvalidoException {
        if (h == null || h.isEmpty()) throw new DadoInvalidoException("A senha não pode ficar vazia.");
        this.senhaHash = h;
    }
    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) throws DadoInvalidoException {
        if (perfil == null) throw new DadoInvalidoException("O perfil é obrigatório.");
        this.perfil = perfil;
    }
}
