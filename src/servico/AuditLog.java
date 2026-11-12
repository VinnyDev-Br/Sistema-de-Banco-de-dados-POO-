package servico;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Log de auditoria: grava em log.txt quem fez o quê e quando. Falha no log nunca derruba o sistema. */
public class AuditLog {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final Path arquivo;

    public AuditLog(Path pasta) { this.arquivo = pasta.resolve("log.txt"); }

    public synchronized void registrar(String usuario, String acao, String detalhe) {
        try (BufferedWriter w = Files.newBufferedWriter(arquivo, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            w.write(LocalDateTime.now().format(FORMATO) + " | " + usuario + " | " + acao + " | " + detalhe);
            w.newLine();
        } catch (IOException e) {
            System.err.println("Não foi possível gravar o log: " + e.getMessage());
        }
    }
}
