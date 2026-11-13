package servico;

import excecao.*;
import modelo.*;
import repositorio.ArquivoCsv;
import repositorio.Repositorio;
import repositorio.RepositorioPessoas;
import util.Texto;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Camada de serviço: concentra a lógica (permissões, validação de duplicidade, gravação, auditoria, backup).
 * As telas só chamam estes métodos e mostram o resultado.
 */
public class SistemaService {
    private static final DateTimeFormatter CARIMBO = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final int MAX_BACKUPS = 20;

    private final Path pasta;
    private final RepositorioPessoas repo = new RepositorioPessoas();
    private final Repositorio armazenamento;
    private final AuditLog log;
    private final Usuario usuario;
    private boolean backupDaSessaoFeito = false;
    private final List<String> avisosDeCarga = new ArrayList<>();

    public SistemaService(Path pasta, Usuario usuario) throws SistemaException {
        this.pasta = pasta;
        this.usuario = usuario;
        this.log = new AuditLog(pasta);
        ArquivoCsv csv = new ArquivoCsv(pasta);
        this.armazenamento = csv;
        boolean primeiraExecucao = !csv.arquivoExiste();
        try {
            repo.substituirTudo(armazenamento.carregar());
            avisosDeCarga.addAll(armazenamento.getAvisos());
            if (primeiraExecucao) {                                  // dados fictícios para teste
                for (Pessoa p : DadosExemplo.gerar()) repo.adicionar(p);
                salvar();
                log.registrar(usuario.getLogin(), "EXEMPLO", "Dados fictícios gerados na primeira execução");
            }
        } catch (IOException e) {
            throw new ArmazenamentoException("Não foi possível carregar os dados: " + e.getMessage(), e);
        }
        log.registrar(usuario.getLogin(), "LOGIN", "Perfil " + usuario.getPerfil() + ", " + repo.tamanho() + " registros");
    }

    public Usuario getUsuario() { return usuario; }
    public boolean podeAlterar() { return usuario.getPerfil().podeAlterar(); }
    public List<String> getAvisosDeCarga() { return avisosDeCarga; }
    public RepositorioPessoas getRepositorio() { return repo; }
    public Path getPasta() { return pasta; }

    private void exigirAdmin() throws AcessoNegadoException {
        if (!podeAlterar()) throw new AcessoNegadoException();
    }

    // ---------------- consultas ----------------
    public List<Pessoa> listarTodos() { return repo.listarTodos(); }

    public List<String> setores() {
        return repo.listarTodos().stream().map(Pessoa::getSetor).distinct()
                .sorted(Comparator.comparing(Texto::normalizar)).collect(Collectors.toList());
    }

    /** Filtros combinados: tipo, setor, situação e texto (nome ou NIP). Parâmetro null = sem filtro. */
    public List<Pessoa> filtrar(String tipo, String setor, Situacao situacao, String texto) {
        String chave = Texto.normalizar(texto);
        return repo.listarTodos().stream()
                .filter(p -> tipo == null || p.getTipo().equals(tipo))
                .filter(p -> setor == null || p.getSetor().equals(setor))
                .filter(p -> situacao == null || p.getSituacao() == situacao)
                .filter(p -> chave.isEmpty() || Texto.normalizar(p.getNome()).contains(chave)
                        || String.valueOf(p.getNip()).contains(chave))
                .collect(Collectors.toList());
    }

    // ---------------- alterações (só ADMIN) ----------------
    public void cadastrar(Pessoa p) throws SistemaException {
        exigirAdmin();
        repo.adicionar(p);
        salvarComAuditoria("CADASTRO", p);
    }

    /** Substitui o cadastro de mesmo NIP, preservando as ocorrências. */
    public void atualizar(Pessoa nova) throws SistemaException {
        exigirAdmin();
        Pessoa antiga = repo.buscar(nova.getNip());
        for (Ocorrencia o : antiga.getOcorrencias()) nova.adicionarOcorrencia(o);
        repo.atualizar(nova);
        Pessoa.registrarRemocao();                                   // a versão antiga deixou de existir
        salvarComAuditoria("EDICAO", nova);
    }

    public void excluir(int nip) throws SistemaException {
        exigirAdmin();
        Pessoa removida = repo.remover(nip);
        salvarComAuditoria("EXCLUSAO", removida);
    }

    public void adicionarOcorrencia(int nip, Ocorrencia o) throws SistemaException {
        exigirAdmin();
        Pessoa p = repo.buscar(nip);
        p.adicionarOcorrencia(o);
        salvar();
        log.registrar(usuario.getLogin(), "OCORRENCIA+", "NIP " + nip + " - " + o.getTipo() + ": " + o.getDescricao());
    }

    public void removerOcorrencia(int nip, int idOcorrencia) throws SistemaException {
        exigirAdmin();
        Pessoa p = repo.buscar(nip);
        if (p.removerOcorrencia(idOcorrencia)) {
            salvar();
            log.registrar(usuario.getLogin(), "OCORRENCIA-", "NIP " + nip + " - ocorrência " + idOcorrencia);
        }
    }

    private void salvarComAuditoria(String acao, Pessoa p) throws ArmazenamentoException {
        salvar();
        log.registrar(usuario.getLogin(), acao, "NIP " + p.getNip() + " - " + p.getNome());
    }

    // ---------------- arquivos ----------------
    /** Grava os dados. Antes da primeira gravação de cada sessão faz uma cópia de segurança. */
    public void salvar() throws ArmazenamentoException {
        try {
            if (!backupDaSessaoFeito) { fazerBackup(); backupDaSessaoFeito = true; }
            armazenamento.salvar(repo.listarTodos());
        } catch (IOException e) {
            throw new ArmazenamentoException("Falha ao gravar os dados: " + e.getMessage(), e);
        }
    }

    /** Copia os arquivos de dados para dados/backups com data e hora no nome. */
    public Path fazerBackup() throws ArmazenamentoException {
        try {
            Path destino = pasta.resolve("backups");
            Files.createDirectories(destino);
            String carimbo = CARIMBO.format(LocalDateTime.now());
            for (Path origem : armazenamento.arquivos()) {
                if (Files.exists(origem)) {
                    String nome = origem.getFileName().toString().replace(".csv", "");
                    Files.copy(origem, destino.resolve(nome + "_" + carimbo + ".csv"), StandardCopyOption.REPLACE_EXISTING);
                }
            }
            podarBackups(destino);
            log.registrar(usuario.getLogin(), "BACKUP", carimbo);
            return destino;
        } catch (IOException e) {
            throw new ArmazenamentoException("Falha ao criar o backup: " + e.getMessage(), e);
        }
    }

    private void podarBackups(Path destino) throws IOException {
        try (Stream<Path> s = Files.list(destino)) {
            List<Path> antigos = s.filter(f -> f.getFileName().toString().endsWith(".csv"))
                    .sorted(Comparator.comparing((Path f) -> f.getFileName().toString()).reversed())
                    .skip(MAX_BACKUPS * 2L).collect(Collectors.toList());     // 2 arquivos por backup
            for (Path f : antigos) Files.deleteIfExists(f);
        }
    }

    /** Resultado da importação de uma planilha CSV. */
    public static class ResultadoImportacao {
        public int importadas, duplicadas;
        public final List<String> erros = new ArrayList<>();
    }

    /** Importa pessoas de um CSV (mesmo formato do sistema, separado por ';'). Linhas ruins são ignoradas e relatadas. */
    public ResultadoImportacao importar(Path arquivo) throws SistemaException {
        exigirAdmin();
        ResultadoImportacao r = new ResultadoImportacao();
        try {
            for (String[] item : ArquivoCsv.lerLinhas(arquivo)) {
                try {
                    repo.adicionar(ArquivoCsv.daLinha(item[1]));
                    r.importadas++;
                } catch (MatriculaDuplicadaException e) {
                    r.duplicadas++;
                    Pessoa.registrarRemocao();
                } catch (DadoInvalidoException e) {
                    r.erros.add("Linha " + item[0] + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new ArmazenamentoException("Não foi possível ler o arquivo: " + e.getMessage(), e);
        }
        if (r.importadas > 0) {
            salvar();
            log.registrar(usuario.getLogin(), "IMPORTACAO", arquivo.getFileName() + ": " + r.importadas + " registros");
        }
        return r;
    }

    /** Exporta a consulta atual para CSV (com BOM para o Excel reconhecer os acentos). */
    public int exportar(Collection<Pessoa> pessoas, Path arquivo) throws ArmazenamentoException {
        try (BufferedWriter w = Files.newBufferedWriter(arquivo, StandardCharsets.UTF_8)) {
            w.write('\uFEFF');
            w.write(ArquivoCsv.CABECALHO_PESSOAS); w.newLine();
            for (Pessoa p : pessoas) { w.write(p.toCsv()); w.newLine(); }
        } catch (IOException e) {
            throw new ArmazenamentoException("Falha ao exportar: " + e.getMessage(), e);
        }
        log.registrar(usuario.getLogin(), "EXPORTACAO", arquivo.getFileName() + ": " + pessoas.size() + " registros");
        return pessoas.size();
    }

    public void registrarSaida() { log.registrar(usuario.getLogin(), "SAIDA", "Sistema encerrado"); }
    
    /** Turmas distintas dos alunos, em ordem alfabética (Alfa, Bravo, Charlie...). */
    public List<String> turmas() {
        return repo.listarTodos().stream()
                .filter(p -> p instanceof Aluno)
                .map(p -> ((Aluno) p).getTurma())
                .distinct()
                .sorted(Comparator.comparing(Texto::normalizar))
                .collect(Collectors.toList());
    }
}
