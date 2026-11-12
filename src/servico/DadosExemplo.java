package servico;

import excecao.DadoInvalidoException;
import modelo.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Dados FICTÍCIOS para testar o sistema (nomes e NIPs inventados). */
public final class DadosExemplo {
    private DadosExemplo() { }

    /** Data de nascimento no mês atual (para o relatório de aniversariantes ter o que mostrar). */
    private static LocalDate esteMes(int ano, int dia) { return LocalDate.of(ano, LocalDate.now().getMonth(), dia); }

    public static List<Pessoa> gerar() throws DadoInvalidoException {
        List<Pessoa> l = new ArrayList<>();
        String cia = "Companhia de Alunos";

        l.add(new Aluno(20260001, "Lucas Almeida Souza", cia, Situacao.ATIVO, LocalDate.of(2007, 3, 14), "2026/A", "1º", 19, 68.5, "(85) 99999-0001"));
        l.add(new Aluno(20260002, "Rafael Costa Lima", cia, Situacao.ATIVO, esteMes(2006, 18), "2026/A", "1º", 20, 72.0, "(85) 99999-0002"));
        l.add(new Aluno(20260003, "Mateus Ferreira Rocha", cia, Situacao.AFASTADO, LocalDate.of(2007, 7, 2), "2026/A", "2º", 19, 65.2, "(88) 98888-0003"));
        l.add(new Aluno(20260004, "Pedro Henrique Nunes", cia, Situacao.ATIVO, LocalDate.of(2006, 12, 25), "2026/B", "3º", 19, 80.4, "(85) 97777-0004"));
        l.add(new Aluno(20260005, "João Victor Barbosa", cia, Situacao.ATIVO, esteMes(2007, 27), "2026/B", "3º", 19, 70.0, "(85) 97777-0005"));
        l.add(new Aluno(20260006, "Gabriel Mendes Pinto", cia, Situacao.DESLIGADO, LocalDate.of(2005, 1, 30), "2025/B", "2º", 21, 75.8, "(88) 96666-0006"));
        l.add(new Aluno(20260007, "Thiago Araújo Cavalcante", cia, Situacao.ATIVO, LocalDate.of(2007, 9, 9), "2026/B", "2º", 19, 66.9, "(85) 95555-0007"));

        l.add(new Instrutor(10010001, "Carlos Eduardo Menezes", "Seção de Ensino", Situacao.ATIVO, LocalDate.of(1988, 5, 20), "Navegação", "1SG"));
        l.add(new Instrutor(10010002, "Antônio Marcos Ribeiro", "Seção de Ensino", Situacao.ATIVO, esteMes(1985, 5), "Marinharia", "SO"));
        l.add(new Instrutor(10010003, "Fábio Henrique Lopes", "Educação Física", Situacao.ATIVO, LocalDate.of(1992, 11, 30), "Treinamento físico", "3SG"));
        l.add(new Instrutor(10010004, "Ricardo Souza Teixeira", "Educação Física", Situacao.AFASTADO, LocalDate.of(1990, 2, 8), "Natação", "2SG"));

        l.add(new Funcionario(30010001, "Maria das Graças Oliveira", "Secretaria", Situacao.ATIVO, LocalDate.of(1979, 6, 12), "Auxiliar administrativa"));
        l.add(new Funcionario(30010002, "José Raimundo Pereira", "Rancho", Situacao.ATIVO, esteMes(1975, 22), "Cozinheiro"));
        l.add(new Funcionario(30010003, "Ana Paula Moreira", "Departamento de Saúde", Situacao.ATIVO, LocalDate.of(1983, 10, 3), "Enfermeira"));

        // ocorrências de exemplo
        LocalDate hoje = LocalDate.now();
        l.get(0).adicionarOcorrencia(new Ocorrencia(hoje.minusDays(40), TipoOcorrencia.ELOGIO, "Destaque na instrução de marinharia.", "3SG Fábio Lopes"));
        l.get(1).adicionarOcorrencia(new Ocorrencia(hoje.minusDays(25), TipoOcorrencia.NEGATIVA, "Atraso à formatura matinal.", "1SG Carlos Menezes"));
        l.get(1).adicionarOcorrencia(new Ocorrencia(hoje.minusDays(10), TipoOcorrencia.NEGATIVA, "Uniforme incompleto na revista.", "SO Antônio Ribeiro"));
        l.get(1).adicionarOcorrencia(new Ocorrencia(hoje.minusDays(3), TipoOcorrencia.ELOGIO, "Ajudou colega durante a faina.", "1SG Carlos Menezes"));
        l.get(2).adicionarOcorrencia(new Ocorrencia(hoje.minusDays(60), TipoOcorrencia.PARTE_OCORRENCIA, "Afastado por orientação médica após lesão no treino.", "Enf. Ana Moreira"));
        l.get(5).adicionarOcorrencia(new Ocorrencia(hoje.minusDays(90), TipoOcorrencia.PARTE_OCORRENCIA, "Desligamento a pedido do aluno.", "SO Antônio Ribeiro"));
        l.get(3).adicionarOcorrencia(new Ocorrencia(hoje.minusDays(15), TipoOcorrencia.ELOGIO, "Melhor desempenho no teste de natação.", "3SG Fábio Lopes"));
        return l;
    }
}
