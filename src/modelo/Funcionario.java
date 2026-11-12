package modelo;

import excecao.DadoInvalidoException;
import util.Csv;

import java.time.LocalDate;

public class Funcionario extends Pessoa {
    private String cargo;

    public Funcionario() { super(); }

    public Funcionario(int nip, String nome, String setor, Situacao situacao, LocalDate nascimento, String cargo)
            throws DadoInvalidoException {
        super(nip, nome, setor, situacao, nascimento);
        try {
            setCargo(cargo);
        } catch (DadoInvalidoException e) {
            registrarRemocao();   // objeto inválido não conta como cadastrado
            throw e;
        }
    }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) throws DadoInvalidoException { this.cargo = exigirTexto(cargo, "Cargo", 60); }

    @Override public String getTipo() { return "FUNCIONARIO"; }

    @Override public String getDescricao() { return "Funcionário - cargo: " + cargo; }

    @Override public String toCsv() { return camposComuns() + ";" + Csv.juntar(cargo); }
}
