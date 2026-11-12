package repositorio;

import excecao.MatriculaDuplicadaException;
import excecao.PessoaNaoEncontradaException;
import modelo.Pessoa;
import modelo.Situacao;
import util.Texto;

import java.util.*;

/** "Banco de dados" em memória: HashMap com chave = NIP (busca rápida). */
public class RepositorioPessoas {
    private final Map<Integer, Pessoa> mapa = new HashMap<>();

    public void adicionar(Pessoa p) throws MatriculaDuplicadaException {
        if (mapa.containsKey(p.getNip())) throw new MatriculaDuplicadaException(p.getNip());
        mapa.put(p.getNip(), p);
    }

    public Pessoa remover(int nip) throws PessoaNaoEncontradaException {
        Pessoa removida = mapa.remove(nip);
        if (removida == null) throw new PessoaNaoEncontradaException(nip);
        Pessoa.registrarRemocao();
        return removida;
    }

    /** Substitui o cadastro que tem o mesmo NIP. */
    public void atualizar(Pessoa nova) throws PessoaNaoEncontradaException {
        if (!mapa.containsKey(nova.getNip())) throw new PessoaNaoEncontradaException(nova.getNip());
        mapa.put(nova.getNip(), nova);
    }

    public boolean existe(int nip) { return mapa.containsKey(nip); }
    public int tamanho() { return mapa.size(); }

    /** Todas as pessoas (Aluno, Instrutor e Funcionario) numa lista só: POLIMORFISMO. */
    public List<Pessoa> listarTodos() {
        List<Pessoa> lista = new ArrayList<>(mapa.values());
        lista.sort(Comparator.comparing(p -> Texto.normalizar(p.getNome())));
        return lista;
    }

    public void substituirTudo(Collection<Pessoa> pessoas) {
        mapa.clear();
        for (Pessoa p : pessoas) mapa.put(p.getNip(), p);
    }

    // ======= SOBRECARGA de buscar() =======

    /** Busca exata por NIP. */
    public Pessoa buscar(int nip) throws PessoaNaoEncontradaException {
        Pessoa p = mapa.get(nip);
        if (p == null) throw new PessoaNaoEncontradaException(nip);
        return p;
    }

    /** Busca por parte do nome (ignora maiúsculas e acentos). */
    public List<Pessoa> buscar(String nome) {
        String chave = Texto.normalizar(nome);
        List<Pessoa> achadas = new ArrayList<>();
        for (Pessoa p : listarTodos()) if (Texto.normalizar(p.getNome()).contains(chave)) achadas.add(p);
        return achadas;
    }

    /** Busca por setor e situação (texto vazio ou null = qualquer). */
    public List<Pessoa> buscar(String setor, String situacao) {
        String s = Texto.normalizar(setor);
        List<Pessoa> achadas = new ArrayList<>();
        for (Pessoa p : listarTodos()) {
            boolean okSetor = s.isEmpty() || Texto.normalizar(p.getSetor()).equals(s);
            boolean okSit = situacao == null || situacao.trim().isEmpty() || p.getSituacao() == Situacao.de(situacao);
            if (okSetor && okSit) achadas.add(p);
        }
        return achadas;
    }
}
