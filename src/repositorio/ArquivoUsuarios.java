package repositorio;

import excecao.ArmazenamentoException;
import excecao.DadoInvalidoException;
import modelo.Perfil;
import modelo.Usuario;
import util.Csv;
import util.Senha;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Lê/grava usuarios.csv (login;hashSenha;perfil). Cria usuários padrão se o arquivo não existir. */
public class ArquivoUsuarios {
    private final Path arquivo;

    public ArquivoUsuarios(Path pasta) { this.arquivo = pasta.resolve("usuarios.csv"); }

    public List<Usuario> carregar() throws ArmazenamentoException {
        try {
            if (Files.notExists(arquivo)) criarPadrao();
            List<Usuario> lista = new ArrayList<>();
            for (String[] item : ArquivoCsv.lerLinhas(arquivo)) {
                List<String> c = Csv.dividir(item[1]);
                try {
                    if (c.size() != 3) throw new DadoInvalidoException("colunas");
                    lista.add(new Usuario(c.get(0), c.get(1), Perfil.valueOf(c.get(2).trim().toUpperCase())));
                } catch (DadoInvalidoException | IllegalArgumentException e) {
                    System.err.println("usuarios.csv, linha " + item[0] + " ignorada.");
                }
            }
            return lista;
        } catch (IOException | DadoInvalidoException e) {
            throw new ArmazenamentoException("Não foi possível ler os usuários: " + e.getMessage(), e);
        }
    }

    private void criarPadrao() throws IOException, DadoInvalidoException {
        Files.createDirectories(arquivo.getParent());
        try (BufferedWriter w = Files.newBufferedWriter(arquivo, StandardCharsets.UTF_8)) {
            w.write("# login;hash SHA-256 da senha;perfil (ADMIN ou CONSULTA)"); w.newLine();
            w.write(Csv.juntar("admin", Senha.hash("admin123"), Perfil.ADMIN)); w.newLine();
            w.write(Csv.juntar("consulta", Senha.hash("consulta123"), Perfil.CONSULTA)); w.newLine();
        }
    }
}
