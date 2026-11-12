package servico;

import modelo.Pessoa;

import java.util.*;
import java.util.stream.Collectors;

/** Relatórios calculados com Map e Stream. */
public final class Relatorios {
    private Relatorios() { }

    public static Map<String, Long> porSetor(Collection<Pessoa> pessoas) {
        return pessoas.stream().collect(Collectors.groupingBy(Pessoa::getSetor, TreeMap::new, Collectors.counting()));
    }

    public static Map<String, Long> porSituacao(Collection<Pessoa> pessoas) {
        return pessoas.stream().collect(Collectors.groupingBy(p -> p.getSituacao().toString(), TreeMap::new, Collectors.counting()));
    }

    public static Map<String, Long> porTipo(Collection<Pessoa> pessoas) {
        return pessoas.stream().collect(Collectors.groupingBy(Pessoa::getTipo, TreeMap::new, Collectors.counting()));
    }

    public static List<Pessoa> aniversariantesDoMes(Collection<Pessoa> pessoas, int mes) {
        return pessoas.stream()
                .filter(p -> p.getDataNascimento() != null && p.getDataNascimento().getMonthValue() == mes)
                .sorted(Comparator.comparingInt(p -> p.getDataNascimento().getDayOfMonth()))
                .collect(Collectors.toList());
    }

    public static List<Pessoa> maisOcorrencias(Collection<Pessoa> pessoas, int limite) {
        return pessoas.stream()
                .filter(p -> !p.getOcorrencias().isEmpty())
                .sorted(Comparator.comparingInt((Pessoa p) -> p.getOcorrencias().size()).reversed()
                        .thenComparing(Pessoa::getNome))
                .limit(limite)
                .collect(Collectors.toList());
    }
}
