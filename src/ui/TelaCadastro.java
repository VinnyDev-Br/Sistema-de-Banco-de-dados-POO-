package ui;

import excecao.DadoInvalidoException;
import excecao.SistemaException;
import modelo.*;
import servico.SistemaService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

/** Formulário (JDialog) de cadastro e edição. Os campos específicos mudam conforme o tipo escolhido. */
public class TelaCadastro extends JDialog {
    private static final String[] TIPOS = {"Aluno", "Instrutor", "Funcionário"};
    private static final String[] CARTAS = {"ALUNO", "INSTRUTOR", "FUNCIONARIO"};
    private static final String[] GRADUACOES = {
        // Praças
        "AM", "GR", "MN", "CB", "3SG", "2SG", "1SG", "SO",
        // Oficiais
        "GM", "2T", "1T", "CT", "CC", "CF", "CMG", "CA", "VA", "AE"};
    }  
    private static final String[] TURMAS = {
        "ALFA 2008", "BRAVO 2009", "CHARLIE 2010", "DELTA 2011",
        "ECHO 2012", "FOXTROT 2013", "GOLF 2014", "HOTEL 2015",
        "INDIA 2016", "JULIET 2017", "KILO 2018", "LIMA 2019",
        "MIKE 2020", "NOVEMBER 2021", "OSCAR 2022", "PAPA 2023",
        "QUEBEC 2024", "ROMEO 2025", "SIERRA 2026", "TANGO 2027",
        "UNIFORM 2028", "VICTOR 2029", "WHISKEY 2030", "X-RAY 2031",
        "YANKEE 2032"
    };
    private final SistemaService servico;
    private final Pessoa existente;
    private boolean salvou = false;

    private final JComboBox<String> tipo = new JComboBox<>(TIPOS);
    private final JTextField nip = new JTextField(12);
    private final JTextField nome = new JTextField(24);
    private final JComboBox<String> setor = new JComboBox<>();
    private final JComboBox<Situacao> situacao = new JComboBox<>(Situacao.values());
    private final JTextField nascimento = new JTextField(10);

    private final JTextField pelotao = new JTextField(12),
        idade = new JTextField(5), peso = new JTextField(6), parente = new JTextField(14);
    private final JComboBox<String> turma = new JComboBox<>(TURMAS);
    private final JTextField especialidade = new JTextField(18);
    private final JComboBox<String> graduacao = new JComboBox<>(GRADUACOES);
    private final JTextField cargo = new JTextField(18);

    private final CardLayout cartas = new CardLayout();
    private final JPanel painelEspecifico = new JPanel(cartas);

    public TelaCadastro(Frame dono, SistemaService servico, Pessoa existente) {
        super(dono, existente == null ? "Novo cadastro" : "Editar cadastro", true);
        this.servico = servico;
        this.existente = existente;

        setor.setEditable(true);
        for (String s : servico.setores()) setor.addItem(s);
        graduacao.setEditable(true);
        turma.setEditable(true);
        JPanel comum = new JPanel(new GridBagLayout());
        Campos.linha(comum, 0, "Tipo:", tipo);
        Campos.linha(comum, 1, "NIP:", nip);
        Campos.linha(comum, 2, "Nome:", nome);
        Campos.linha(comum, 3, "Setor:", setor);
        Campos.linha(comum, 4, "Situação:", situacao);
        Campos.linha(comum, 5, "Nascimento (dd/mm/aaaa):", nascimento);

        JPanel pAluno = new JPanel(new GridBagLayout());
        Campos.linha(pAluno, 0, "Turma:", turma);
        Campos.linha(pAluno, 1, "Pelotão:", pelotao);
        Campos.linha(pAluno, 2, "Idade:", idade);
        Campos.linha(pAluno, 3, "Peso (kg):", peso);
        Campos.linha(pAluno, 4, "Nº parentesco (telefone):", parente);
        JPanel pInstrutor = new JPanel(new GridBagLayout());
        Campos.linha(pInstrutor, 0, "Especialidade:", especialidade);
        Campos.linha(pInstrutor, 1, "Graduação:", graduacao);
        JPanel pFunc = new JPanel(new GridBagLayout());
        Campos.linha(pFunc, 0, "Cargo:", cargo);

        painelEspecifico.add(pAluno, CARTAS[0]);
        painelEspecifico.add(pInstrutor, CARTAS[1]);
        painelEspecifico.add(pFunc, CARTAS[2]);
        painelEspecifico.setBorder(BorderFactory.createTitledBorder("Dados específicos"));
        tipo.addActionListener(e -> {
            cartas.show(painelEspecifico, CARTAS[tipo.getSelectedIndex()]);
            pack();
        });

        JButton ok = new JButton("Salvar");
        JButton cancelar = new JButton("Cancelar");
        ok.addActionListener(e -> salvar());
        cancelar.addActionListener(e -> dispose());
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botoes.add(ok); botoes.add(cancelar);
        getRootPane().setDefaultButton(ok);

        JPanel centro = new JPanel(new BorderLayout());
        centro.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        centro.add(comum, BorderLayout.NORTH);
        centro.add(painelEspecifico, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);

        if (existente != null) preencher(existente);
        cartas.show(painelEspecifico, CARTAS[tipo.getSelectedIndex()]);
        pack();
        setMinimumSize(new Dimension(460, getHeight()));
        setLocationRelativeTo(dono);
    }

    private void preencher(Pessoa p) {
        tipo.setSelectedIndex(p instanceof Aluno ? 0 : p instanceof Instrutor ? 1 : 2);
        tipo.setEnabled(false);                       // não se troca o tipo ao editar
        nip.setText(String.valueOf(p.getNip()));
        nip.setEnabled(false);                        // NIP é a chave: não muda
        nome.setText(p.getNome());
        setor.setSelectedItem(p.getSetor());
        situacao.setSelectedItem(p.getSituacao());
        if (p.getDataNascimento() != null) nascimento.setText(p.getDataNascimento().format(Campos.DATA));
        if (p instanceof Aluno) {
            Aluno a = (Aluno) p;
            turma.setSelectedItem(a.getTurma()); pelotao.setText(a.getPelotao());
            idade.setText(String.valueOf(a.getIdade())); peso.setText(String.valueOf(a.getPeso()).replace('.', ','));
            parente.setText(a.getNumeroParentesco());
        } else if (p instanceof Instrutor) {
            Instrutor i = (Instrutor) p;
            especialidade.setText(i.getEspecialidade()); graduacao.setSelectedItem(i.getGraduacao());
        } else {
            cargo.setText(((Funcionario) p).getCargo());
        }
    }

    /** Monta um objeto novo (valida tudo no construtor) e entrega ao serviço. */
    private void salvar() {
        try {
            int numero = Campos.inteiro(nip, "NIP");
            String nomeTxt = nome.getText();
            Object s = setor.getSelectedItem();
            String setorTxt = s == null ? "" : s.toString();
            Situacao sit = (Situacao) situacao.getSelectedItem();
            LocalDate nasc = Campos.data(nascimento, "Nascimento", false);
            String turmaSel = turma.getSelectedItem() == null ? "" : turma.getSelectedItem().toString();
            Pessoa p;
            switch (tipo.getSelectedIndex()) {
                case 0:
                    p = new Aluno(numero, nomeTxt, setorTxt, sit, nasc, turmaSel, pelotao.getText(),
                    Campos.inteiro(idade, "Idade"), Campos.decimal(peso, "Peso"), parente.getText());
                    break;
                case 1:
                    Object g = graduacao.getSelectedItem();
                    p = new Instrutor(numero, nomeTxt, setorTxt, sit, nasc, especialidade.getText(), g == null ? "" : g.toString());
                    break;
                default:
                    p = new Funcionario(numero, nomeTxt, setorTxt, sit, nasc, cargo.getText());
            }
            if (existente == null) servico.cadastrar(p); else servico.atualizar(p);
            salvou = true;
            dispose();
        } catch (SistemaException ex) {                 // DadoInvalido, MatriculaDuplicada, Armazenamento...
            Campos.erro(this, ex.getMessage());
        }
    }

    public boolean salvou() { return salvou; }
}
