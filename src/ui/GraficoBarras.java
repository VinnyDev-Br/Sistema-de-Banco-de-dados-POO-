package ui;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/** Gráfico de barras desenhado à mão com Graphics2D (usado no relatório por setor). */
public class GraficoBarras extends JPanel {
    private static final Color[] CORES = {
            new Color(0x2E6DA4), new Color(0x3A9D5D), new Color(0xD98E04), new Color(0x8E5BA8),
            new Color(0xC2453D), new Color(0x2A9CA8), new Color(0x7F8C3A)};
    private final Map<String, Long> dados;
    private final String titulo;

    public GraficoBarras(String titulo, Map<String, Long> dados) {
        this.titulo = titulo;
        this.dados = dados;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(640, 380));
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        g.setColor(Color.DARK_GRAY);
        g.setFont(getFont().deriveFont(Font.BOLD, 14f));
        g.drawString(titulo, 20, 24);

        if (dados.isEmpty()) { g.setFont(getFont().deriveFont(13f)); g.drawString("Sem dados.", 20, 60); g.dispose(); return; }

        int esq = 50, dir = 20, topo = 50, base = 70;
        int areaW = w - esq - dir, areaH = h - topo - base;
        long max = dados.values().stream().mapToLong(Long::longValue).max().orElse(1);
        long passoEixo = Math.max(1, (long) Math.ceil(max / 5.0));
        long teto = Math.max(1, ((max + passoEixo - 1) / passoEixo) * passoEixo);

        // eixos e linhas de grade
        g.setFont(getFont().deriveFont(11f));
        int passos = (int) (teto / passoEixo);
        for (int i = 0; i <= passos; i++) {
            int y = topo + areaH - (int) (areaH * (double) i / passos);
            g.setColor(new Color(0xE0E0E0));
            g.drawLine(esq, y, esq + areaW, y);
            g.setColor(Color.GRAY);
            String rotulo = String.valueOf(Math.round((double) teto * i / passos));
            g.drawString(rotulo, esq - 8 - g.getFontMetrics().stringWidth(rotulo), y + 4);
        }
        g.setColor(Color.DARK_GRAY);
        g.drawLine(esq, topo, esq, topo + areaH);
        g.drawLine(esq, topo + areaH, esq + areaW, topo + areaH);

        int n = dados.size();
        int faixa = areaW / n;
        int larguraBarra = Math.max(12, Math.min(70, faixa - 16));
        int i = 0;
        for (Map.Entry<String, Long> e : dados.entrySet()) {
            int altura = (int) (areaH * (double) e.getValue() / teto);
            int x = esq + i * faixa + (faixa - larguraBarra) / 2;
            int y = topo + areaH - altura;
            g.setColor(CORES[i % CORES.length]);
            g.fillRoundRect(x, y, larguraBarra, altura, 6, 6);

            g.setColor(Color.BLACK);
            g.setFont(getFont().deriveFont(Font.BOLD, 12f));
            String valor = String.valueOf(e.getValue());
            g.drawString(valor, x + (larguraBarra - g.getFontMetrics().stringWidth(valor)) / 2, y - 5);

            g.setFont(getFont().deriveFont(11f));
            String nome = abreviar(e.getKey(), g.getFontMetrics(), faixa - 4);
            g.drawString(nome, esq + i * faixa + (faixa - g.getFontMetrics().stringWidth(nome)) / 2, topo + areaH + 18);
            i++;
        }
        g.dispose();
    }

    private static String abreviar(String s, FontMetrics fm, int larguraMax) {
        if (fm.stringWidth(s) <= larguraMax) return s;
        while (s.length() > 1 && fm.stringWidth(s + "…") > larguraMax) s = s.substring(0, s.length() - 1);
        return s + "…";
    }
}
