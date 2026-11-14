package ui;

import excecao.SistemaException;
import modelo.Pessoa;
import modelo.Situacao;
import servico.SistemaService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Path;

/** Janela principal: tabela de pessoas, filtros combinados em tempo real e botões de CRUD. */
public class TelaPrincipal extends JFrame {
    private static final String TODOS = "Todos";
    private static final String[] TIPOS_ROTULO = {TODOS, "Aluno", "Instrutor", "Funcionário"};
    private static final String[] TIPOS_CHAVE = {null, "ALUNO", "INSTRUTOR", "FUNCIONARIO"};

    private final SistemaService servico;
    private final PessoaTableModel modelo = new PessoaTableModel();
    private final JTable tabela = new JTable(modelo);

    private final JTextField busca = new JTextField(18);
    private final JComboBox<String> filtroTurma = new JComboBox<>();
    private final JComboBox<String> filtroTipo = new JComboBox<>(TIPOS_ROTULO);
    private final JComboBox<String> filtroSetor = new JComboBox<>();
    private final JComboBox<Object> filtroSituacao = new JComboBox<>();
    private final JLabel status = new JLabel(" ");
    private boolean atualizandoFiltros = false;

    private final JButton btnNovo = new JButton("Novo");
    private final JButton btnEditar = new JButton("Editar");
    private final JButton btnExcluir = new JButton("Excluir");
    private final JButton btnDetalhes = new JButton("Detalhes");

    public TelaPrincipal(SistemaService servico) {
        super("Cadastro Geral - Escola de Aprendizes-Marinheiros do Ceará");
        this.servico = servico;

        filtroSituacao.addItem("Todas");
        for (Situacao s : Situacao.values()) filtroSituacao.addItem(s);

        montarMenu();
        add(montarFiltros(), BorderLayout.NORTH);
        add(montarTabela(), BorderLayout.CENTER);
        add(montarRodape(), BorderLayout.SOUTH);

        boolean admin = servico.podeAlterar();
        btnNovo.setEnabled(admin); btnEditar.setEnabled(admin); btnExcluir.setEnabled(admin);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { sair(); }
        });
        recarregarSetores();
        recarregarTurmas();
        atualizarTabela();
        setSize(1050, 600);
        setLocationRelativeTo(null);
    }

    // ---------- montagem ----------
    private JPanel montarFiltros() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        p.add(new JLabel("Buscar (nome ou NIP):")); p.add(busca);
        p.add(new JLabel("Tipo:")); p.add(filtroTipo);
        p.add(new JLabel("Setor:")); p.add(filtroSetor);
        p.add(new JLabel("Turma:")); p.add(filtroTurma);
        p.add(new JLabel("Situação:")); p.add(filtroSituacao);
        JButton limpar = new JButton("Limpar filtros");
        limpar.addActionListener(e -> {
            atualizandoFiltros = true;
            busca.setText(""); filtroTipo.setSelectedIndex(0);
            filtroSetor.setSelectedIndex(0); filtroTurma.setSelectedIndex(0); filtroSituacao.setSelectedIndex(0);
            atualizandoFiltros = false;
            atualizarTabela();
        });
        p.add(limpar);

        busca.getDocument().addDocumentListener(new DocumentListener() {   // busca em tempo real
            public void insertUpdate(DocumentEvent e) { atualizarTabela(); }
            public void removeUpdate(DocumentEvent e) { atualizarTabela(); }
            public void changedUpdate(DocumentEvent e) { atualizarTabela(); }
        });
        filtroTipo.addActionListener(e -> atualizarTabela());
        filtroSetor.addActionListener(e -> atualizarTabela());
        filtroSituacao.addActionListener(e -> atualizarTabela());
        filtroTurma.addActionListener(e -> atualizarTabela());
        return p;
    }

    private JScrollPane montarTabela() {
        tabela.setRowHeight(26);
        tabela.setAutoCreateRowSorter(true);                 // clicar no cabeçalho ordena
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setFillsViewportHeight(true);
        int[] larguras = {90, 230, 90, 170, 90, 380};
        for (int i = 0; i < larguras.length; i++) tabela.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        tabela.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) detalhes(); }
        });
        return new JScrollPane(tabela);
    }

    private JPanel montarRodape() {
        btnNovo.addActionListener(e -> novo());
        btnEditar.addActionListener(e -> editar());
        btnExcluir.addActionListener(e -> excluir());
        btnDetalhes.addActionListener(e -> detalhes());
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        botoes.add(btnNovo); botoes.add(btnEditar); botoes.add(btnExcluir); botoes.add(btnDetalhes);

        status.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.add(botoes, BorderLayout.WEST);
        rodape.add(status, BorderLayout.EAST);
        return rodape;
    }

    private void montarMenu() {
        JMenuBar barra = new JMenuBar();

        JMenu arquivo = new JMenu("Arquivo");
        JMenuItem importar = new JMenuItem("Importar CSV...");
        JMenuItem exportar = new JMenuItem("Exportar consulta atual...");
        JMenuItem salvar = new JMenuItem("Salvar agora");
        JMenuItem backup = new JMenuItem("Fazer backup agora");
        JMenuItem sair = new JMenuItem("Sair");
        importar.setEnabled(servico.podeAlterar());
        importar.addActionListener(e -> importar());
        exportar.addActionListener(e -> exportar());
        salvar.addActionListener(e -> executar(() -> { servico.salvar(); JOptionPane.showMessageDialog(this, "Dados salvos."); }));
        backup.addActionListener(e -> executar(() -> {
            Path destino = servico.fazerBackup();
            JOptionPane.showMessageDialog(this, "Backup criado em:\n" + destino.toAbsolutePath());
        }));
        sair.addActionListener(e -> sair());
        salvar.setAccelerator(KeyStroke.getKeyStroke("control S"));
        arquivo.add(importar); arquivo.add(exportar); arquivo.addSeparator();
        arquivo.add(salvar); arquivo.add(backup); arquivo.addSeparator(); arquivo.add(sair);

        JMenu relatorios = new JMenu("Relatórios");
        relatorios.add(itemRelatorio("Resumo (tipo, situação e setor)", TelaRelatorios.RESUMO));
        relatorios.add(itemRelatorio("Gráfico por setor", TelaRelatorios.GRAFICO));
        relatorios.add(itemRelatorio("Aniversariantes do mês", TelaRelatorios.ANIVERSARIANTES));
        relatorios.add(itemRelatorio("Pessoas com mais ocorrências", TelaRelatorios.OCORRENCIAS));

        JMenu ajuda = new JMenu("Ajuda");
        JMenuItem sobre = new JMenuItem("Sobre");
        sobre.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Cadastro Geral - Escola de Aprendizes-Marinheiros do Ceará\n"
                        + "Projeto de Programação Orientada a Objetos (Java + Swing)\n\n"
                        + "Dados gravados em: " + servico.getPasta().toAbsolutePath() + "\n"
                        + "Atalhos: duplo clique abre os detalhes; clique no cabeçalho ordena; Ctrl+S salva.",
                "Sobre", JOptionPane.INFORMATION_MESSAGE));
        ajuda.add(sobre);

        barra.add(arquivo); barra.add(relatorios); barra.add(ajuda);
        setJMenuBar(barra);
    }

    private JMenuItem itemRelatorio(String texto, int aba) {
        JMenuItem item = new JMenuItem(texto);
        item.addActionListener(e -> new TelaRelatorios(this, servico, aba).setVisible(true));
        return item;
    }

    // ---------- dados da tabela ----------
    private void atualizarTabela() {
        if (atualizandoFiltros) return;
        String setor = filtroSetor.getSelectedIndex() <= 0 ? null : (String) filtroSetor.getSelectedItem();
        String turma = filtroTurma.getSelectedIndex() <= 0 ? null : (String) filtroTurma.getSelectedItem();   // <- novo
        Object sit = filtroSituacao.getSelectedItem();
        Situacao situacao = sit instanceof Situacao ? (Situacao) sit : null;
        int selecionado = pessoaSelecionada() == null ? -1 : pessoaSelecionada().getNip();

        modelo.setDados(servico.filtrar(TIPOS_CHAVE[filtroTipo.getSelectedIndex()], setor, turma, situacao, busca.getText()));   // <- mudou

        for (int i = 0; i < tabela.getRowCount(); i++) {              // mantém a seleção após atualizar
            if ((Integer) tabela.getValueAt(i, 0) == selecionado) { tabela.setRowSelectionInterval(i, i); break; }
        }
        status.setText(String.format("Usuário: %s (%s)  |  Exibindo %d de %d  |  Total cadastrado nesta sessão: %d",
                servico.getUsuario().getLogin(), servico.getUsuario().getPerfil(),
                modelo.getRowCount(), servico.getRepositorio().tamanho(), Pessoa.getTotalCadastrados()));
    }

    private void recarregarSetores() {
        atualizandoFiltros = true;
        Object anterior = filtroSetor.getSelectedItem();
        filtroSetor.removeAllItems();
        filtroSetor.addItem(TODOS);
        for (String s : servico.setores()) filtroSetor.addItem(s);
        if (anterior != null) filtroSetor.setSelectedItem(anterior);
        if (filtroSetor.getSelectedIndex() < 0) filtroSetor.setSelectedIndex(0);
        atualizandoFiltros = false;
    }

    private void recarregarTurmas() {
        atualizandoFiltros = true;
        Object anterior = filtroTurma.getSelectedItem();
        filtroTurma.removeAllItems();
        filtroTurma.addItem(TODOS);
        for (String t : servico.turmas()) filtroTurma.addItem(t);
        if (anterior != null) filtroTurma.setSelectedItem(anterior);
        if (filtroTurma.getSelectedIndex() < 0) filtroTurma.setSelectedIndex(0);
        atualizandoFiltros = false;
}

    private void depoisDeAlterar() { recarregarSetores(); recarregarTurmas(); atualizarTabela(); }

    private Pessoa pessoaSelecionada() {
        int linha = tabela.getSelectedRow();
        return linha < 0 ? null : modelo.get(tabela.convertRowIndexToModel(linha));
    }

    private Pessoa exigirSelecao() {
        Pessoa p = pessoaSelecionada();
        if (p == null) Campos.erro(this, "Selecione uma pessoa na tabela.");
        return p;
    }

    // ---------- ações ----------
    private void novo() {
        TelaCadastro t = new TelaCadastro(this, servico, null);
        t.setVisible(true);
        if (t.salvou()) depoisDeAlterar();
    }

    private void editar() {
        Pessoa p = exigirSelecao();
        if (p == null) return;
        TelaCadastro t = new TelaCadastro(this, servico, p);
        t.setVisible(true);
        if (t.salvou()) depoisDeAlterar();
    }

    private void excluir() {
        Pessoa p = exigirSelecao();
        if (p == null) return;
        int r = JOptionPane.showConfirmDialog(this,
                "Excluir " + p.getNome() + " (NIP " + p.getNip() + ")?\nEsta ação não pode ser desfeita.",
                "Confirmar exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.YES_OPTION) return;
        executar(() -> { servico.excluir(p.getNip()); depoisDeAlterar(); });
    }

    private void detalhes() {
        Pessoa p = exigirSelecao();
        if (p == null) return;
        executar(() -> { new TelaDetalhes(this, servico, p.getNip()).setVisible(true); depoisDeAlterar(); });
    }

    private void importar() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Escolha o arquivo CSV (separado por ponto e vírgula)");
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        executar(() -> {
            SistemaService.ResultadoImportacao r = servico.importar(fc.getSelectedFile().toPath());
            StringBuilder msg = new StringBuilder(r.importadas + " registro(s) importado(s).");
            if (r.duplicadas > 0) msg.append("\n").append(r.duplicadas).append(" ignorado(s) por NIP já existente.");
            if (!r.erros.isEmpty()) {
                msg.append("\n\n").append(r.erros.size()).append(" linha(s) com problema:\n");
                r.erros.stream().limit(8).forEach(s -> msg.append("• ").append(s).append('\n'));
                if (r.erros.size() > 8) msg.append("• ...");
            }
            JOptionPane.showMessageDialog(this, msg.toString(), "Importação", JOptionPane.INFORMATION_MESSAGE);
            depoisDeAlterar();
        });
    }

    private void exportar() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("consulta.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File destino = fc.getSelectedFile();
        if (!destino.getName().toLowerCase().endsWith(".csv")) destino = new File(destino.getPath() + ".csv");
        File arquivo = destino;
        executar(() -> {
            int n = servico.exportar(modelo.getDados(), arquivo.toPath());
            JOptionPane.showMessageDialog(this, n + " registro(s) exportado(s) para:\n" + arquivo.getAbsolutePath());
        });
    }

    private void sair() {
        try {
            servico.salvar();
        } catch (SistemaException e) {
            int r = JOptionPane.showConfirmDialog(this, e.getMessage() + "\n\nSair mesmo assim?", "Erro ao salvar",
                    JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
            if (r != JOptionPane.YES_OPTION) return;
        }
        servico.registrarSaida();
        dispose();
        System.exit(0);
    }

    // ---------- tratamento de erro único para as ações ----------
    @FunctionalInterface private interface Acao { void rodar() throws SistemaException; }

    /** try/catch na camada que fala com o usuário: nenhum erro derruba o programa. */
    private void executar(Acao acao) {
        try {
            acao.rodar();
        } catch (SistemaException e) {
            Campos.erro(this, e.getMessage());
        } catch (RuntimeException e) {
            Campos.erro(this, "Erro inesperado: " + e);
        }
    }
}
