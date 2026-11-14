package ui;

import excecao.DadoInvalidoException;
import modelo.Usuario;
import servico.ServicoAutenticacao;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class TelaLogin extends JDialog {
    private final JTextField campoLogin = new JTextField(20);
    private final JPasswordField campoSenha = new JPasswordField(20);
    private final ServicoAutenticacao auth;
    private Usuario autenticado;

    private TelaLogin(ServicoAutenticacao auth) {
        super((Frame) null, "Acesso ao sistema", true);
        this.auth = auth;

        PainelFundo fundo = new PainelFundo(carregar("/recursos/fundo.jpg"));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridwidth = 2;

        // logo da escola (se existir)
        Image logo = carregar("/recursos/logo.png");
        if (logo != null) {
            g.gridy = 0; g.insets = new Insets(20, 5, 10, 5);
            fundo.add(new JLabel(new ImageIcon(logo.getScaledInstance(-1, 130, Image.SCALE_SMOOTH))), g);
        }

        // título: BANCO DE DADOS + EAMCE
        JLabel titulo = new JLabel("BANCO DE DADOS");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 24f));
        titulo.setForeground(Color.WHITE);
        g.gridy = 1; g.insets = new Insets(4, 5, 0, 5);
        fundo.add(titulo, g);

        JLabel sigla = new JLabel("EAMCE");
        sigla.setFont(sigla.getFont().deriveFont(Font.PLAIN, 34f));
        sigla.setForeground(new Color(0xBFD8FF));
        g.gridy = 2; g.insets = new Insets(0, 5, 2, 5);
        fundo.add(sigla, g);

        JLabel sub = new JLabel("Escola de Aprendizes-Marinheiros do Ceará");
        sub.setFont(sub.getFont().deriveFont(Font.PLAIN, 13f));
        sub.setForeground(Color.WHITE);
        g.gridy = 3; g.insets = new Insets(0, 5, 22, 5);
        fundo.add(sub, g);

        // campos
        linha(fundo, 4, "Login:", campoLogin);
        linha(fundo, 5, "Senha:", campoSenha);

        // botões
        JButton entrar = new JButton("Entrar");
        JButton sair = new JButton("Sair");
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        botoes.setOpaque(false);
        botoes.add(entrar); botoes.add(sair);
        g.gridx = 0; g.gridy = 6; g.gridwidth = 2; g.anchor = GridBagConstraints.EAST;
        g.insets = new Insets(12, 5, 5, 5);
        fundo.add(botoes, g);

        entrar.addActionListener(e -> tentar());
        sair.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(entrar);

        setContentPane(fundo);
        setSize(520, 560);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private static void linha(JPanel p, int y, String rotulo, JComponent campo) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = y; g.insets = new Insets(5, 5, 5, 5);
        g.gridx = 0; g.anchor = GridBagConstraints.EAST;
        JLabel l = new JLabel(rotulo);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 13f));
        l.setForeground(Color.WHITE);
        p.add(l, g);
        g.gridx = 1; g.anchor = GridBagConstraints.WEST;
        p.add(campo, g);
    }

    private static Image carregar(String caminho) {
        URL url = TelaLogin.class.getResource(caminho);
        return url == null ? null : new ImageIcon(url).getImage();
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

    /** Painel que desenha a imagem de fundo esticada (ou um degradê azul se a imagem não existir). */
    private static class PainelFundo extends JPanel {
        private final Image imagem;

        PainelFundo(Image imagem) {
            this.imagem = imagem;
            setLayout(new GridBagLayout());
        }

        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            if (imagem != null) {
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.drawImage(imagem, 0, 0, getWidth(), getHeight(), this);
            } else {
                g.setPaint(new GradientPaint(0, 0, new Color(0x0B2A5B), 0, getHeight(), new Color(0x2E6DA4)));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            g.dispose();
        }
    }
}