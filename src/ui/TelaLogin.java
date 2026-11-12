package ui;

import excecao.DadoInvalidoException;
import modelo.Usuario;
import servico.ServicoAutenticacao;

import javax.swing.*;
import java.awt.*;

public class TelaLogin extends JDialog {
    private final JTextField campoLogin = new JTextField(16);
    private final JPasswordField campoSenha = new JPasswordField(16);
    private final ServicoAutenticacao auth;
    private Usuario autenticado;

    private TelaLogin(ServicoAutenticacao auth) {
        super((Frame) null, "Acesso ao sistema", true);
        this.auth = auth;

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5); g.anchor = GridBagConstraints.WEST;
        JLabel titulo = new JLabel("Cadastro Geral – Escola de Aprendizes-Marinheiros do Ceará");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 13f));
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2; form.add(titulo, g);
        g.gridwidth = 1; g.gridy = 1; form.add(new JLabel("Login:"), g);
        g.gridx = 1; form.add(campoLogin, g);
        g.gridx = 0; g.gridy = 2; form.add(new JLabel("Senha:"), g);
        g.gridx = 1; form.add(campoSenha, g);

        JButton entrar = new JButton("Entrar");
        JButton sair = new JButton("Sair");
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botoes.add(entrar); botoes.add(sair);

        entrar.addActionListener(e -> tentar());
        sair.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(entrar);

        add(form, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);
        pack();
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void tentar() {
        char[] senha = campoSenha.getPassword();
        try {
            autenticado = auth.autenticar(campoLogin.getText(), senha);
            dispose();
        } catch (DadoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Acesso negado", JOptionPane.ERROR_MESSAGE);
            campoSenha.setText("");
            campoSenha.requestFocus();
        } finally {
            java.util.Arrays.fill(senha, '\0');
        }
    }

    /** Mostra a tela e devolve o usuário autenticado (ou null se o usuário desistiu). */
    public static Usuario pedir(ServicoAutenticacao auth) {
        TelaLogin t = new TelaLogin(auth);
        t.setVisible(true);
        return t.autenticado;
    }
}
