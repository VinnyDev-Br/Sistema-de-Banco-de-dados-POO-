package modelo;

public enum Perfil {
    ADMIN, CONSULTA;

    public boolean podeAlterar() { return this == ADMIN; }
}
