package modelo;

import excecao.DadoInvalidoException;

import java.time.LocalDate;

/** Registro de elogio, negativa ou parte de ocorrência associado a uma pessoa. */
public class Ocorrencia {
    private static int proximoId = 1;          // atributo de classe: próximo ID

    private int id;
    private LocalDate data;
    private TipoOcorrencia tipo;
    private String descricao;
    private String aplicador;

    public Ocorrencia() { this.id = proximoId++; this.data = LocalDate.now(); this.tipo = TipoOcorrencia.ELOGIO; }

    public Ocorrencia(LocalDate data, TipoOcorrencia tipo, String descricao, String aplicador)
            throws DadoInvalidoException {
        setData(data); setTipo(tipo); setDescricao(descricao); setAplicador(aplicador);
        this.id = proximoId++;
    }

    /** Usado ao carregar do arquivo: mantém o ID gravado e ajusta o contador. */
    public Ocorrencia(int id, LocalDate data, TipoOcorrencia tipo, String descricao, String aplicador)
            throws DadoInvalidoException {
        setData(data); setTipo(tipo); setDescricao(descricao); setAplicador(aplicador);
        this.id = id;
        if (id >= proximoId) proximoId = id + 1;
    }

    public int getId() { return id; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) throws DadoInvalidoException {
        if (data == null || data.isAfter(LocalDate.now())) throw new DadoInvalidoException("Data da ocorrência inválida.");
        this.data = data;
    }
    public TipoOcorrencia getTipo() { return tipo; }
    public void setTipo(TipoOcorrencia tipo) throws DadoInvalidoException {
        if (tipo == null) throw new DadoInvalidoException("O tipo da ocorrência é obrigatório.");
        this.tipo = tipo;
    }
    public String getDescricao() { return descricao; }
    public void setDescricao(String d) throws DadoInvalidoException { this.descricao = exigir(d, "Descrição"); }
    public String getAplicador() { return aplicador; }
    public void setAplicador(String a) throws DadoInvalidoException { this.aplicador = exigir(a, "Aplicador"); }

    private static String exigir(String v, String campo) throws DadoInvalidoException {
        if (v == null || v.trim().isEmpty()) throw new DadoInvalidoException("O campo \"" + campo + "\" não pode ficar vazio.");
        return v.trim();
    }
}
