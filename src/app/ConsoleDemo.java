package app;

import excecao.*;
import modelo.*;
import repositorio.RepositorioPessoas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Demonstração no console (Fases 1 a 4): herança, polimorfismo, sobrecarga, static e exceções. */
public class ConsoleDemo {
    public static void main(String[] args) {
        // Pessoa p = new Pessoa();   // NÃO compila: Pessoa é abstrata

        List<Pessoa> lista = new ArrayList<>();                      // polimorfismo: uma lista, vários tipos
        try {
            lista.add(new Aluno(1001, "Lucas Almeida", "Companhia de Alunos", Situacao.ATIVO, null, "A", "1º Pel", 19, 68.5, "(85) 99999-0001"));
            lista.add(new Instrutor(2001, "Carlos Menezes", "Seção de Ensino", Situacao.ATIVO, null, "Navegação", "1SG"));
            lista.add(new Funcionario(3001, "Maria Oliveira", "Secretaria", Situacao.ATIVO, null, "Auxiliar administrativa"));
        } catch (DadoInvalidoException e) {
            System.out.println("Erro: " + e.getMessage());
        }

        System.out.println("== getDescricao() sobrescrito em cada subclasse ==");
        for (Pessoa p : lista) System.out.println(p + "  ->  " + p.getDescricao());
        System.out.println("\n== toCsv() ==");
        for (Pessoa p : lista) System.out.println(p.toCsv());
        System.out.println("\nTotal cadastrado (static): " + Pessoa.getTotalCadastrados());

        RepositorioPessoas repo = new RepositorioPessoas();
        for (Pessoa p : lista) {
            try { repo.adicionar(p); } catch (MatriculaDuplicadaException e) { System.out.println(e.getMessage()); }
        }

        System.out.println("\n== Sobrecarga de buscar() ==");
        try {
            System.out.println("buscar(2001)             -> " + repo.buscar(2001));
        } catch (PessoaNaoEncontradaException e) { System.out.println(e.getMessage()); }
        System.out.println("buscar(\"maria\")          -> " + repo.buscar("maria"));
        System.out.println("buscar(\"Secretaria\",\"ativo\") -> " + repo.buscar("Secretaria", "ativo"));

        System.out.println("\n== Exceções personalizadas ==");
        try { repo.adicionar(new Funcionario(1001, "Outro Nome", "Rancho", Situacao.ATIVO, null, "Cozinheiro")); }
        catch (MatriculaDuplicadaException | DadoInvalidoException e) { System.out.println("Capturada: " + e.getMessage()); }
        try { new Aluno(-5, "", "X", Situacao.ATIVO, LocalDate.now(), "A", "B", 19, 70, ""); }
        catch (DadoInvalidoException e) { System.out.println("Capturada: " + e.getMessage()); }
        try { repo.buscar(999); }
        catch (PessoaNaoEncontradaException e) { System.out.println("Capturada: " + e.getMessage()); }
        System.out.println("Total (static) - o NIP duplicado foi criado mas rejeitado; os dados inválidos não contam: " + Pessoa.getTotalCadastrados());
    }
}
