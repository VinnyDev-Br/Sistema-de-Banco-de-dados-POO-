import modelo.*;
import servico.*;
import ui.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.*;

public class CapturaTelas {
    static void foto(Window w, String nome) throws Exception {
        w.pack();
        BufferedImage img = new BufferedImage(w.getWidth(), w.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        w.paint(g);
        g.dispose();
        ImageIO.write(img, "png", new File("/tmp/" + nome + ".png"));
    }

    public static void main(String[] args) throws Exception {
        for (UIManager.LookAndFeelInfo i : UIManager.getInstalledLookAndFeels())
            if ("Nimbus".equals(i.getName())) UIManager.setLookAndFeel(i.getClassName());
        Path pasta = Files.createTempDirectory("bd-gui");
        Usuario admin = new ServicoAutenticacao(pasta).autenticar("admin", "admin123".toCharArray());
        SistemaService s = new SistemaService(pasta, admin);

        SwingUtilities.invokeAndWait(() -> {
            try {
                TelaPrincipal t = new TelaPrincipal(s);
                t.setSize(1050, 520);
                t.setVisible(true);
                t.validate();
                BufferedImage img = new BufferedImage(t.getWidth(), t.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g = img.createGraphics(); t.paint(g); g.dispose();
                ImageIO.write(img, "png", new File("/tmp/tela_principal.png"));

                TelaCadastro c = new TelaCadastro(t, s, null); c.setModal(false); c.setVisible(true); foto(c, "tela_cadastro");
                TelaDetalhes d = new TelaDetalhes(t, s, 20260002); d.setModal(false); d.setVisible(true); d.setSize(760, 520); d.validate(); foto(d, "tela_detalhes");
                d.setSize(760, 520);
                TelaRelatorios r = new TelaRelatorios(t, s, TelaRelatorios.GRAFICO); r.setModal(false); r.setVisible(true); r.setSize(700, 500); r.validate(); foto(r, "tela_grafico");
                r.setSize(700, 500);
            } catch (Exception e) { e.printStackTrace(); }
        });
        System.exit(0);
    }
}
