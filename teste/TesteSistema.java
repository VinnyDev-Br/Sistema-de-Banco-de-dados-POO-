import excecao.*;
import modelo.*;
import servico.*;

import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;

/** Teste automático simples (sem JUnit): java -cp out:teste-out TesteSistema */
public class TesteSistema {
    static int falhas = 0;
    static void confere(String nome, boolean ok) { System.out.println((ok ? "[OK]    " : "[FALHA] ") + nome); if (!ok) falhas++; }

    public static void main(String[] a) throws Exception {
        Path pasta = Files.createTempDirectory("bd-teste");
        ServicoAutenticacao auth = new ServicoAutenticacao(pasta);
        Usuario admin = auth.autenticar("admin", "admin123".toCharArray());
        Usuario cons = auth.autenticar("consulta", "consulta123".toCharArray());
        try { auth.autenticar("admin", "errada".toCharArray()); confere("senha errada rejeitada", false); }
        catch (DadoInvalidoException e) { confere("senha errada rejeitada", true); }

        SistemaService s = new SistemaService(pasta, admin);
        confere("dados de exemplo gerados (14)", s.listarTodos().size() == 14);
        confere("filtro por tipo ALUNO = 7", s.filtrar("ALUNO", null, null, "").size() == 7);
        confere("filtro combinado aluno+ativo", s.filtrar("ALUNO", null, Situacao.ATIVO, "").size() == 5);
        confere("busca sem acento: 'joao' acha João", s.filtrar(null, null, null, "joao").size() == 1);
        confere("busca por NIP", s.filtrar(null, null, null, "30010002").size() == 1);

        s.cadastrar(new Funcionario(555, "Teste Novo", "Rancho", Situacao.ATIVO, null, "Auxiliar"));
        try { s.cadastrar(new Funcionario(555, "Outro", "Rancho", Situacao.ATIVO, null, "X")); confere("NIP duplicado rejeitado", false); }
        catch (MatriculaDuplicadaException e) { confere("NIP duplicado rejeitado", true); }

        s.atualizar(new Funcionario(555, "Teste Editado", "Secretaria", Situacao.AFASTADO, null, "Chefe"));
        confere("edição aplicada", s.getRepositorio().buscar(555).getNome().equals("Teste Editado"));
        s.adicionarOcorrencia(555, new Ocorrencia(LocalDate.now(), TipoOcorrencia.ELOGIO, "Boa conduta; com ponto e vírgula\ne quebra de linha", "SO Fulano"));
        s.atualizar(new Funcionario(555, "Teste Editado", "Secretaria", Situacao.ATIVO, null, "Chefe"));
        confere("ocorrência preservada após editar", s.getRepositorio().buscar(555).getOcorrencias().size() == 1);

        // persistência: abre de novo e compara
        SistemaService s2 = new SistemaService(pasta, admin);
        confere("persistiu entre execuções (15)", s2.listarTodos().size() == 15);
        Ocorrencia o = s2.getRepositorio().buscar(555).getOcorrencias().get(0);
        confere("escape de ; e \\n na ocorrência", o.getDescricao().equals("Boa conduta; com ponto e vírgula\ne quebra de linha"));
        confere("ocorrências de exemplo recarregadas", s2.getRepositorio().buscar(20260002).getOcorrencias().size() == 3);

        s2.excluir(555);
        try { s2.excluir(555); confere("excluir inexistente lança exceção", false); }
        catch (PessoaNaoEncontradaException e) { confere("excluir inexistente lança exceção", true); }

        // perfil CONSULTA
        SistemaService c = new SistemaService(pasta, cons);
        try { c.excluir(20260001); confere("CONSULTA não exclui", false); }
        catch (AcessoNegadoException e) { confere("CONSULTA não exclui", true); }

        // importação com linhas ruins
        Path csv = pasta.resolve("planilha.csv");
        Files.write(csv, List.of(
                "ALUNO;9001;Importado Um;Companhia de Alunos;ATIVO;;T1;P1;18;70,5;(85) 1111",
                "FUNCIONARIO;9002;Importado Dois;Rancho;ativo;2000-05-10;Cozinheiro",
                "ALUNO;9001;Repetido;Cia;ATIVO;;T;P;18;70;",
                "ALUNO;9003;Idade ruim;Cia;ATIVO;;T;P;abc;70;",
                "XYZ;1;a;b;c;d",
                "FUNCIONARIO;9004;;Rancho;ATIVO;;Cargo"));
        SistemaService.ResultadoImportacao r = s2.importar(csv);
        confere("importou 2, 1 duplicada, 3 erros", r.importadas == 2 && r.duplicadas == 1 && r.erros.size() == 3);

        // arquivo mal formatado não derruba o carregamento
        Files.writeString(pasta.resolve("pessoas.csv"),
                Files.readString(pasta.resolve("pessoas.csv")) + "lixo sem sentido\nALUNO;1;2\n");
        SistemaService s3 = new SistemaService(pasta, admin);
        confere("linhas ruins ignoradas e avisadas (2)", s3.getAvisosDeCarga().size() == 2);

        Path exp = pasta.resolve("exp.csv");
        confere("exportação", s3.exportar(s3.filtrar("ALUNO", null, null, ""), exp) == s3.filtrar("ALUNO", null, null, "").size());

        confere("backup criado", Files.list(pasta.resolve("backups")).count() >= 2);
        confere("log de auditoria existe", Files.readString(pasta.resolve("log.txt")).contains("EXCLUSAO"));
        confere("relatório por setor", Relatorios.porSetor(s3.listarTodos()).get("Companhia de Alunos") >= 7);
        confere("aniversariantes do mês atual", Relatorios.aniversariantesDoMes(s3.listarTodos(), LocalDate.now().getMonthValue()).size() >= 3);
        confere("top ocorrências começa por quem tem mais", Relatorios.maisOcorrencias(s3.listarTodos(), 3).get(0).getNip() == 20260002);

        System.out.println(falhas == 0 ? "\nTODOS OS TESTES PASSARAM" : "\nFALHAS: " + falhas);
        System.exit(falhas);
    }
}
