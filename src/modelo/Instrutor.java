package modelo;

import excecao.DadoInvalidoException;
import util.Csv;

import java.time.LocalDate;

public class Instrutor extends Pessoa {
    private String especialidade;
    private String graduacao;      // 3SG, 2SG, 1SG, SO...

    public Instrutor() { super(); }

    public Instrutor(int nip, String nome, String setor, Situacao situacao, LocalDate nascimento,
                     String especialidade, String graduacao) throws DadoInvalidoException {
        super(nip, nome, setor, situacao, nascimento);
        try {
            setEspecialidade(especialidade);
            setGraduacao(graduacao);
        } catch (DadoInvalidoException e) {
            registrarRemocao();   // objeto inválido não conta como cadastrado
            throw e;
        }
    }

    public String getEspecialidade() { return especialidade; }
    public void setEspecialidade(String e) throws DadoInvalidoException { this.especialidade = exigirTexto(e, "Especialidade", 60); }

    public String getGraduacao() { return graduacao; }
    public void setGraduacao(String g) throws DadoInvalidoException { this.graduacao = exigirTexto(g, "Graduação", 30); }

    @Override public String getTipo() { return "INSTRUTOR"; }

    @Override public String getDescricao() {
        return "Instrutor - " + graduacao + ", especialidade: " + especialidade;
    }

    @Override public String toCsv() {
        return camposComuns() + ";" + Csv.juntar(especialidade, graduacao);
    }
}
