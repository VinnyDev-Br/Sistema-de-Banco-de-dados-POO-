package repositorio;

import excecao.DadoInvalidoException;
import modelo.*;
import util.Csv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

/** Persistência em arquivos CSV (pessoas.csv e ocorrencias.csv) com BufferedReader/BufferedWriter. */
public class ArquivoCsv implements Repositorio {
    public static final String CABECALHO_PESSOAS =
            "# TIPO;NIP;NOME;SETOR;SITUACAO;NASCIMENTO;(ALUNO: TURMA;PELOTAO;IDADE;PESO;NUM_PARENTE)"
            + "(INSTRUTOR: ESPECIALIDADE;GRADUACAO)(FUNCIONARIO: CARGO)";

    private final Path arqPessoas;
    private final Path arqOcorrencias;
    private final List<String> avisos = new ArrayList<>();

    public ArquivoCsv(Path pasta) {
        this.arqPessoas = pasta.resolve("pessoas.csv");
        this.arqOcorrencias = pasta.resolve("ocorrencias.csv");
    }

    public boolean arquivoExiste() { return Files.exists(arqPessoas); }

    @Override public List<String> getAvisos() { return avisos; }
    @Override public List<Path> arquivos() { return List.of(arqPessoas, arqOcorrencias); }

    @Override
    public List<Pessoa> carregar() throws IOException {
        avisos.clear();
        Files.createDirectories(arqPessoas.getParent());
        if (Files.notExists(arqPessoas)) Files.createFile(arqPessoas);        // cria se não existir
        if (Files.notExists(arqOcorrencias)) Files.createFile(arqOcorrencias);

        Map<Integer, Pessoa> porNip = new LinkedHashMap<>();
        for (String[] item : lerLinhas(arqPessoas)) {
            try {
                Pessoa p = daLinha(item[1]);
                if (porNip.containsKey(p.getNip())) throw new DadoInvalidoException("NIP repetido " + p.getNip());
                porNip.put(p.getNip(), p);
            } catch (DadoInvalidoException e) {
                avisos.add("pessoas.csv, linha " + item[0] + ": " + e.getMessage());
            }
        }
        for (String[] item : lerLinhas(arqOcorrencias)) {
            try {
                List<String> c = Csv.dividir(item[1]);
                if (c.size() != 6) throw new DadoInvalidoException("esperadas 6 colunas");
                Pessoa dono = porNip.get(inteiro(c.get(0), "NIP"));
                if (dono == null) throw new DadoInvalidoException("NIP sem cadastro");
                dono.adicionarOcorrencia(new Ocorrencia(inteiro(c.get(1), "ID"), data(c.get(2)),
                        tipoOcorrencia(c.get(3)), c.get(4), c.get(5)));
            } catch (DadoInvalidoException e) {
                avisos.add("ocorrencias.csv, linha " + item[0] + ": " + e.getMessage());
            }
        }
        return new ArrayList<>(porNip.values());
    }

    @Override
    public void salvar(Collection<Pessoa> pessoas) throws IOException {
        Files.createDirectories(arqPessoas.getParent());
        Path tmpP = Files.createTempFile(arqPessoas.getParent(), "pessoas", ".tmp");
        Path tmpO = Files.createTempFile(arqPessoas.getParent(), "ocorrencias", ".tmp");
        try (BufferedWriter wp = Files.newBufferedWriter(tmpP, StandardCharsets.UTF_8);
             BufferedWriter wo = Files.newBufferedWriter(tmpO, StandardCharsets.UTF_8)) {
            wp.write(CABECALHO_PESSOAS); wp.newLine();
            for (Pessoa p : pessoas) {
                wp.write(p.toCsv()); wp.newLine();
                for (Ocorrencia o : p.getOcorrencias()) {
                    wo.write(Csv.juntar(p.getNip(), o.getId(), o.getData(), o.getTipo().name(),
                            o.getDescricao(), o.getAplicador()));
                    wo.newLine();
                }
            }
        } catch (IOException e) {
            Files.deleteIfExists(tmpP); Files.deleteIfExists(tmpO);
            throw e;
        }
        // só troca os arquivos depois de gravar tudo com sucesso
        Files.move(tmpP, arqPessoas, StandardCopyOption.REPLACE_EXISTING);
        Files.move(tmpO, arqOcorrencias, StandardCopyOption.REPLACE_EXISTING);
    }

    /** Lê um arquivo e devolve pares {numeroDaLinha, texto}, ignorando linhas vazias e comentários (#). */
    public static List<String[]> lerLinhas(Path arquivo) throws IOException {
        List<String[]> saida = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            String linha;
            int n = 0;
            while ((linha = br.readLine()) != null) {
                n++;
                if (n == 1 && linha.startsWith("\uFEFF")) linha = linha.substring(1);   // BOM do Excel
                if (linha.trim().isEmpty() || linha.startsWith("#")) continue;
                saida.add(new String[]{String.valueOf(n), linha});
            }
        }
        return saida;
    }

    /** Lê o primeiro campo (tipo) e decide qual subclasse instanciar. */
    public static Pessoa daLinha(String linha) throws DadoInvalidoException {
        List<String> c = Csv.dividir(linha);
        String tipo = c.get(0).trim().toUpperCase();
        int esperado;
        switch (tipo) {
            case "ALUNO":       esperado = 11; break;
            case "INSTRUTOR":   esperado = 8;  break;
            case "FUNCIONARIO": esperado = 7;  break;
            default: throw new DadoInvalidoException("tipo desconhecido \"" + c.get(0) + "\"");
        }
        if (c.size() != esperado)
            throw new DadoInvalidoException(tipo + " exige " + esperado + " colunas, mas a linha tem " + c.size());

        int nip = inteiro(c.get(1), "NIP");
        Situacao sit;
        try { sit = Situacao.de(c.get(4)); } catch (IllegalArgumentException e) { throw new DadoInvalidoException(e.getMessage()); }
        LocalDate nasc = c.get(5).trim().isEmpty() ? null : data(c.get(5));

        switch (tipo) {
            case "ALUNO":
                return new Aluno(nip, c.get(2), c.get(3), sit, nasc, c.get(6), c.get(7),
                        inteiro(c.get(8), "idade"), decimal(c.get(9), "peso"), c.get(10));
            case "INSTRUTOR":
                return new Instrutor(nip, c.get(2), c.get(3), sit, nasc, c.get(6), c.get(7));
            default:
                return new Funcionario(nip, c.get(2), c.get(3), sit, nasc, c.get(6));
        }
    }

    private static int inteiro(String s, String campo) throws DadoInvalidoException {
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { throw new DadoInvalidoException("\"" + s + "\" não é um número inteiro válido (" + campo + ")"); }
    }
    private static double decimal(String s, String campo) throws DadoInvalidoException {
        try { return Double.parseDouble(s.trim().replace(',', '.')); }
        catch (NumberFormatException e) { throw new DadoInvalidoException("\"" + s + "\" não é um número válido (" + campo + ")"); }
    }
    private static LocalDate data(String s) throws DadoInvalidoException {
        try { return LocalDate.parse(s.trim()); }
        catch (DateTimeParseException e) { throw new DadoInvalidoException("data inválida \"" + s + "\" (use AAAA-MM-DD)"); }
    }
    private static TipoOcorrencia tipoOcorrencia(String s) throws DadoInvalidoException {
        try { return TipoOcorrencia.valueOf(s.trim().toUpperCase()); }
        catch (IllegalArgumentException e) { throw new DadoInvalidoException("tipo de ocorrência inválido \"" + s + "\""); }
    }
}
