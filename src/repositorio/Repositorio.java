package repositorio;

import modelo.Pessoa;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

/**
 * Contrato de persistência. O resto do sistema só conversa com esta interface:
 * para trocar CSV por SQLite basta criar outra classe que a implemente.
 */
public interface Repositorio {
    List<Pessoa> carregar() throws IOException;
    void salvar(Collection<Pessoa> pessoas) throws IOException;
    /** Mensagens sobre linhas ignoradas na última leitura. */
    List<String> getAvisos();
    /** Arquivos de dados (usado no backup). */
    List<Path> arquivos();
}
