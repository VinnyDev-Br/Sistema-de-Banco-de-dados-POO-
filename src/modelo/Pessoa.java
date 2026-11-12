package modelo;

import excecao.DadoInvalidoException;
import util.Csv;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Classe ABSTRATA e ENCAPSULADA: não pode ser instanciada; atributos privados com getters/setters validados.
 * As subclasses (Aluno, Instrutor, Funcionario) herdam os campos comuns e SOBRESCREVEM os métodos abstratos.
 */
public abstract class Pessoa implements Exportavel {

    // ----- atributo de CLASSE (static): contador de registros -----
    private static int totalCadastrados = 0;

    private int nip;
    private String nome;
    private String setor;
    private Situacao situacao;
    private LocalDate dataNascimento;                       // opcional (usada nos aniversariantes)
    private final List<Ocorrencia> ocorrencias = new ArrayList<>();

    /** Construtor vazio (valores padrão; exige preencher via setters). */
    protected Pessoa() {
        this.situacao = Situacao.ATIVO;
        totalCadastrados++;
    }

    /** Construtor completo (valida tudo pelos setters). */
    protected Pessoa(int nip, String nome, String setor, Situacao situacao, LocalDate dataNascimento)
            throws DadoInvalidoException {
        setNip(nip);
        setNome(nome);
        setSetor(setor);
        setSituacao(situacao);
        setDataNascimento(dataNascimento);
        totalCadastrados++;
    }

    // ----- métodos abstratos: cada filha SOBRESCREVE de um jeito -----
    public abstract String getTipo();
    public abstract String getDescricao();
    @Override public abstract String toCsv();

    // ----- getters / setters com validação -----
    public int getNip() { return nip; }
    public void setNip(int nip) throws DadoInvalidoException {
        if (nip <= 0) throw new DadoInvalidoException("O NIP deve ser um número positivo.");
        this.nip = nip;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) throws DadoInvalidoException {
        this.nome = exigirTexto(nome, "Nome", 100);
    }

    public String getSetor() { return setor; }
    public void setSetor(String setor) throws DadoInvalidoException {
        this.setor = exigirTexto(setor, "Setor", 60);
    }

    public Situacao getSituacao() { return situacao; }
    public void setSituacao(Situacao situacao) throws DadoInvalidoException {
        if (situacao == null) throw new DadoInvalidoException("A situação é obrigatória.");
        this.situacao = situacao;
    }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate data) throws DadoInvalidoException {
        if (data != null && (data.isAfter(LocalDate.now()) || data.getYear() < 1900))
            throw new DadoInvalidoException("Data de nascimento inválida.");
        this.dataNascimento = data;
    }

    public List<Ocorrencia> getOcorrencias() { return Collections.unmodifiableList(ocorrencias); }
    public void adicionarOcorrencia(Ocorrencia o) { ocorrencias.add(o); }
    public boolean removerOcorrencia(int idOcorrencia) { return ocorrencias.removeIf(o -> o.getId() == idOcorrencia); }

    // ----- static -----
    public static int getTotalCadastrados() { return totalCadastrados; }
    /** Chamado pelo repositório quando uma pessoa é removida. */
    public static void registrarRemocao() { if (totalCadastrados > 0) totalCadastrados--; }

    // ----- apoio para as subclasses -----
    protected static String exigirTexto(String valor, String campo, int max) throws DadoInvalidoException {
        if (valor == null || valor.trim().isEmpty())
            throw new DadoInvalidoException("O campo \"" + campo + "\" não pode ficar vazio.");
        String v = valor.trim();
        if (v.length() > max)
            throw new DadoInvalidoException("O campo \"" + campo + "\" aceita no máximo " + max + " caracteres.");
        return v;
    }

    /** Campos comuns da linha CSV (tipo, nip, nome, setor, situação, nascimento). */
    protected String camposComuns() {
        return Csv.juntar(getTipo(), nip, nome, setor, situacao.name(),
                dataNascimento == null ? "" : dataNascimento.toString());
    }

    @Override
    public String toString() { return getTipo() + " " + nip + " - " + nome; }
}
