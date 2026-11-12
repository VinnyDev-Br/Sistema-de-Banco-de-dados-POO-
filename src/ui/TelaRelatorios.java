package ui;

import modelo.Pessoa;
import servico.Relatorios;
import servico.SistemaService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TelaRelatorios extends JDialog {
    public static final int RESUMO = 0, GRAFICO = 1, ANIVERSARIANTES = 2, OCORRENCIAS = 3;

    private final SistemaService servico;

    public TelaRelatorios(Frame dono, SistemaService servico, int aba) {
        super(dono, "Relatórios", true);
        this.servico = servico;
        List<Pessoa> todos = servico.listarTodos();

        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Resumo", new JScrollPane(areaTexto(resumo(todos))));
        abas.addTab("Gráfico por setor", new GraficoBarras("Pessoas por setor", Relatorios.porSetor(todos)));
        abas.addTab("Aniversariantes", painelAniversariantes(todos));
        abas.addTab("Mais ocorrências", new JScrollPane(areaTexto(maisOcorrencias(todos))));
        abas.setSelectedIndex(aba);

        JButton fechar = new JButton("Fechar");
        fechar.addActionListener(e -> dispose());
        JPanel sul = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sul.add(fechar);
        add(abas, BorderLayout.CENTER);
        add(sul, BorderLayout.SOUTH);
        setSize(700, 500);
        setLocationRelativeTo(dono);
    }

    private static JTextArea areaTexto(String texto) {
        JTextArea a = new JTextArea(texto);
        a.setEditable(false);
        a.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        a.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        a.setCaretPosition(0);
        return a;
    }

    private static void bloco(StringBuilder sb, String titulo, Map<String, Long> mapa) {
        sb.append(titulo).append('\n');
        for (Map.Entry<String, Long> e : mapa.entrySet()) sb.append(String.format("  %-28s %d%n", e.getKey(), e.getValue()));
        sb.append('\n');
    }

    private String resumo(List<Pessoa> todos) {
        StringBuilder sb = new StringBuilder();
        sb.append("Total de pessoas cadastradas: ").append(todos.size()).append("\n\n");
        bloco(sb, "Por tipo:", Relatorios.porTipo(todos));
        bloco(sb, "Por situação:", Relatorios.porSituacao(todos));
        bloco(sb, "Por setor:", Relatorios.porSetor(todos));
        return sb.toString();
    }

    private String maisOcorrencias(List<Pessoa> todos) {
        List<Pessoa> top = Relatorios.maisOcorrencias(todos, 5);
        if (top.isEmpty()) return "Nenhuma ocorrência registrada.";
        StringBuilder sb = new StringBuilder("Pessoas com mais ocorrências:\n\n");
        int pos = 1;
        for (Pessoa p : top) {
            long neg = p.getOcorrencias().stream().filter(o -> o.getTipo().name().equals("NEGATIVA")).count();
            long elo = p.getOcorrencias().stream().filter(o -> o.getTipo().name().equals("ELOGIO")).count();
            sb.append(String.format("%d. %s (NIP %d) - %d ocorrência(s): %d negativa(s), %d elogio(s)%n",
                    pos++, p.getNome(), p.getNip(), p.getOcorrencias().size(), neg, elo));
        }
        return sb.toString();
    }

    private JPanel painelAniversariantes(List<Pessoa> todos) {
        JComboBox<String> meses = new JComboBox<>();
        for (Month m : Month.values()) meses.addItem(m.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("pt-BR")));
        meses.setSelectedIndex(LocalDate.now().getMonthValue() - 1);

        JTextArea area = areaTexto("");
        Runnable atualizar = () -> {
            List<Pessoa> lista = Relatorios.aniversariantesDoMes(todos, meses.getSelectedIndex() + 1);
            StringBuilder sb = new StringBuilder();
            if (lista.isEmpty()) sb.append("Ninguém faz aniversário neste mês (ou a data de nascimento não foi informada).");
            for (Pessoa p : lista) {
                sb.append(String.format("Dia %02d - %s (%s)%n", p.getDataNascimento().getDayOfMonth(), p.getNome(), p.getTipo()));
            }
            area.setText(sb.toString());
            area.setCaretPosition(0);
        };
        meses.addActionListener(e -> atualizar.run());
        atualizar.run();

        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.add(new JLabel("Mês:"));
        topo.add(meses);
        JPanel p = new JPanel(new BorderLayout());
        p.add(topo, BorderLayout.NORTH);
        p.add(new JScrollPane(area), BorderLayout.CENTER);
        return p;
    }
}
