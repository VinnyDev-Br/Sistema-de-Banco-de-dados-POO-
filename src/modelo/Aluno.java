package modelo;

import excecao.DadoInvalidoException;
import util.Csv;

import java.time.LocalDate;

public class Aluno extends Pessoa {
    private String turma;
    private String pelotao;
    private int idade;
    private double peso;
    private String numeroParentesco;   // telefone do parente de contato

    public Aluno() { super(); }

    public Aluno(int nip, String nome, String setor, Situacao situacao, LocalDate nascimento,
                 String turma, String pelotao, int idade, double peso, String numeroParentesco)
            throws DadoInvalidoException {
        super(nip, nome, setor, situacao, nascimento);
        try {
            setTurma(turma);
            setPelotao(pelotao);
            setIdade(idade);
            setPeso(peso);
            setNumeroParentesco(numeroParentesco);
        } catch (DadoInvalidoException e) {
            registrarRemocao();   // objeto inválido não conta como cadastrado
            throw e;
        }
    }

    public String getTurma() { return turma; }
    public void setTurma(String turma) throws DadoInvalidoException { this.turma = exigirTexto(turma, "Turma", 30); }

    public String getPelotao() { return pelotao; }
    public void setPelotao(String pelotao) throws DadoInvalidoException { this.pelotao = exigirTexto(pelotao, "Pelotão", 30); }

    public int getIdade() { return idade; }
    public void setIdade(int idade) throws DadoInvalidoException {
        if (idade < 12 || idade > 99) throw new DadoInvalidoException("A idade deve estar entre 12 e 99 anos.");
        this.idade = idade;
    }

    public double getPeso() { return peso; }
    public void setPeso(double peso) throws DadoInvalidoException {
        if (peso < 20 || peso > 250) throw new DadoInvalidoException("O peso deve estar entre 20 e 250 kg.");
        this.peso = peso;
    }

    public String getNumeroParentesco() { return numeroParentesco; }
    public void setNumeroParentesco(String numero) throws DadoInvalidoException {
        String n = numero == null ? "" : numero.trim();
        if (!n.matches("[0-9()+\\-\\s]{0,20}"))
            throw new DadoInvalidoException("O número do parente deve conter apenas dígitos, espaços, ( ) + e -.");
        this.numeroParentesco = n;
    }

    @Override public String getTipo() { return "ALUNO"; }

    @Override public String getDescricao() {
        return String.format("Aluno - Turma %s, Pelotão %s, %d anos, %.1f kg", turma, pelotao, idade, peso);
    }

    @Override public String toCsv() {
        return camposComuns() + ";" + Csv.juntar(turma, pelotao, idade, peso, numeroParentesco);
    }
}
