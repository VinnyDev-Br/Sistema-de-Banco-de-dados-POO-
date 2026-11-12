package ui;

import excecao.SistemaException;
import modelo.*;
import servico.SistemaService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.LocalDate;

/** Todos os dados de uma pessoa + lista de ocorrências (duplo clique na tabela principal). */
public class TelaDetalhes extends JDialog {
    private final SistemaService servico;
    private final int nip;
    private final JTextArea dados = new JTextArea(9, 40);
    private final OcorrenciasModel modelo = new OcorrenciasModel();
    private final JTable tabela = new JTable(modelo);
    private final JButton novaOcorrencia = new JButton("Nova ocorrência...");
    private final JButton removerOcorrencia = new JButton("Remover ocorrência");

    public TelaDetalhes(Frame dono, SistemaService servico, int nip) throws SistemaException {
        super(dono, "Detalhes da pessoa", true);
        this.servico = servico;
        this.nip = nip;

        dados.setEditable(false);
        dados.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        dados.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        tabela.setRowHeight(24);
        tabela.setAutoCreateRowSorter(true);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(90);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(140);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(320);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(160);
        JScrollPane scrollOc = new JScrollPane(tabela);
        scrollOc.setBorder(BorderFactory.createTitledBorder("Ocorrências"));

        novaOcorrencia.addActionListener(e -> nova());
        removerOcorrencia.addActionListener(e -> remover());
        novaOcorrencia.setEnabled(servico.podeAlterar());
        removerOcorrencia.setEnabled(servico.podeAlterar());
        JButton fechar = new JButton("Fechar");
        fechar.addActionListener(e -> dispose());

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botoes.add(novaOcorrencia); botoes.add(removerOcorrencia); botoes.add(fechar);

        setLayout(new BorderLayout(4, 4));
        add(new JScrollPane(dados), BorderLayout.NORTH);
        add(scrollOc, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);
        recarregar();
        setSize(760, 520);
        setLocationRelativeTo(dono);
    }

    private void recarregar() {
        try {
            Pessoa p = servico.getRepositorio().buscar(nip);
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-14s %s%n", "NIP:", p.getNip()));
            sb.append(String.format("%-14s %s%n", "Nome:", p.getNome()));
            sb.append(String.format("%-14s %s%n", "Tipo:", p.getTipo()));
            sb.append(String.format("%-14s %s%n", "Setor:", p.getSetor()));
            sb.append(String.format("%-14s %s%n", "Situação:", p.getSituacao()));
            sb.append(String.format("%-14s %s%n", "Nascimento:",
                    p.getDataNascimento() == null ? "-" : p.getDataNascimento().format(Campos.DATA)));
            if (p instanceof Aluno) {
                Aluno a = (Aluno) p;
                sb.append(String.format("%-14s %s / Pelotão %s%n", "Turma/Pelotão:", a.getTurma(), a.getPelotao()));
                sb.append(String.format("%-14s %d anos, %.1f kg%n", "Idade/Peso:", a.getIdade(), a.getPeso()));
                sb.append(String.format("%-14s %s%n", "Nº parente:", a.getNumeroParentesco().isEmpty() ? "-" : a.getNumeroParentesco()));
            } else if (p instanceof Instrutor) {
                Instrutor i = (Instrutor) p;
                sb.append(String.format("%-14s %s%n", "Graduação:", i.getGraduacao()));
                sb.append(String.format("%-14s %s%n", "Especialidade:", i.getEspecialidade()));
            } else {
                sb.append(String.format("%-14s %s%n", "Cargo:", ((Funcionario) p).getCargo()));
            }
            dados.setText(sb.toString());
            dados.setRows(sb.toString().split("\n").length);
            dados.setCaretPosition(0);
            modelo.setLista(p);
        } catch (SistemaException e) {
            Campos.erro(this, e.getMessage());
            dispose();
        }
    }

    private void nova() {
        JTextField data = new JTextField(LocalDate.now().format(Campos.DATA), 10);
        JComboBox<TipoOcorrencia> tipo = new JComboBox<>(TipoOcorrencia.values());
        JTextField descricao = new JTextField(28);
        JTextField aplicador = new JTextField(20);
        JPanel form = new JPanel(new GridBagLayout());
        Campos.linha(form, 0, "Data (dd/mm/aaaa):", data);
        Campos.linha(form, 1, "Tipo:", tipo);
        Campos.linha(form, 2, "Descrição:", descricao);
        Campos.linha(form, 3, "Aplicador:", aplicador);

        while (true) {                                  // repete até estar válido ou o usuário cancelar
            int r = JOptionPane.showConfirmDialog(this, form, "Nova ocorrência", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION) return;
            try {
                Ocorrencia o = new Ocorrencia(Campos.data(data, "Data", true), (TipoOcorrencia) tipo.getSelectedItem(),
                        descricao.getText(), aplicador.getText());
                servico.adicionarOcorrencia(nip, o);
                recarregar();
                return;
            } catch (SistemaException ex) {
                Campos.erro(this, ex.getMessage());
            }
        }
    }

    private void remover() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) { Campos.erro(this, "Selecione uma ocorrência na tabela."); return; }
        Ocorrencia o = modelo.get(tabela.convertRowIndexToModel(linha));
        if (JOptionPane.showConfirmDialog(this, "Remover a ocorrência selecionada?", "Confirmar",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            servico.removerOcorrencia(nip, o.getId());
            recarregar();
        } catch (SistemaException ex) {
            Campos.erro(this, ex.getMessage());
        }
    }

    private static class OcorrenciasModel extends AbstractTableModel {
        private static final String[] COLS = {"Data", "Tipo", "Descrição", "Aplicador"};
        private java.util.List<Ocorrencia> lista = new java.util.ArrayList<>();
        void setLista(Pessoa p) { lista = new java.util.ArrayList<>(p.getOcorrencias()); fireTableDataChanged(); }
        Ocorrencia get(int i) { return lista.get(i); }
        @Override public int getRowCount() { return lista.size(); }
        @Override public int getColumnCount() { return COLS.length; }
        @Override public String getColumnName(int c) { return COLS[c]; }
        @Override public Object getValueAt(int l, int c) {
            Ocorrencia o = lista.get(l);
            switch (c) {
                case 0: return o.getData().format(Campos.DATA);
                case 1: return o.getTipo().toString();
                case 2: return o.getDescricao();
                default: return o.getAplicador();
            }
        }
    }
}
