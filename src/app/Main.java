package app;

import excecao.SistemaException;
import modelo.Usuario;
import servico.ServicoAutenticacao;
import servico.SistemaService;
import ui.TelaLogin;
import ui.TelaPrincipal;

import javax.swing.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Ponto de entrada. Uso: java -jar BancoDados.jar [pasta-de-dados]   (padrão: ./dados) */
public class Main {
    public static void main(String[] args) {
        Path pasta = Paths.get(args.length > 0 ? args[0] : "dados");
        SwingUtilities.invokeLater(() -> iniciar(pasta));
    }

    private static void iniciar(Path pasta) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) { UIManager.setLookAndFeel(info.getClassName()); break; }
            }
        } catch (Exception ignorado) { /* usa o visual padrão */ }

        try {
            ServicoAutenticacao auth = new ServicoAutenticacao(pasta);
            Usuario usuario = TelaLogin.pedir(auth);
            if (usuario == null) System.exit(0);

            SistemaService servico = new SistemaService(pasta, usuario);
            TelaPrincipal tela = new TelaPrincipal(servico);
            tela.setVisible(true);
            if (!servico.getAvisosDeCarga().isEmpty()) {
                JOptionPane.showMessageDialog(tela,
                        servico.getAvisosDeCarga().size() + " linha(s) mal formatada(s) foram ignoradas ao carregar:\n"
                                + String.join("\n", servico.getAvisosDeCarga().subList(0, Math.min(6, servico.getAvisosDeCarga().size()))),
                        "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SistemaException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Erro ao iniciar", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}
