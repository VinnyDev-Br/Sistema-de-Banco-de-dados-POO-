package ui;

import excecao.DadoInvalidoException;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/** Funções de apoio para formulários (leitura de números e datas, montagem de linhas). */
final class Campos {
    static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private Campos() { }

    static int inteiro(JTextField f, String nome) throws DadoInvalidoException {
        try { return Integer.parseInt(f.getText().trim()); }
        catch (NumberFormatException e) { throw new DadoInvalidoException("O campo \"" + nome + "\" precisa ser um número inteiro."); }
    }

    static double decimal(JTextField f, String nome) throws DadoInvalidoException {
        try { return Double.parseDouble(f.getText().trim().replace(',', '.')); }
        catch (NumberFormatException e) { throw new DadoInvalidoException("O campo \"" + nome + "\" precisa ser um número (ex.: 70,5)."); }
    }

    /** Data no formato dd/MM/aaaa. Se obrigatoria=false, vazio devolve null. */
    static LocalDate data(JTextField f, String nome, boolean obrigatoria) throws DadoInvalidoException {
        String t = f.getText().trim();
        if (t.isEmpty()) {
            if (obrigatoria) throw new DadoInvalidoException("O campo \"" + nome + "\" é obrigatório.");
            return null;
        }
        try { return LocalDate.parse(t, DATA); }
        catch (DateTimeParseException e) { throw new DadoInvalidoException("Data inválida em \"" + nome + "\". Use dd/mm/aaaa."); }
    }

    static void linha(JPanel p, int linha, String rotulo, JComponent campo) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = linha; g.anchor = GridBagConstraints.WEST; g.insets = new Insets(4, 6, 4, 6);
        p.add(new JLabel(rotulo), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        p.add(campo, g);
    }

    static void erro(Component pai, String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem, "Atenção", JOptionPane.ERROR_MESSAGE);
    }
}
