package ui;

import modelo.Pessoa;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/** Modelo próprio da JTable: cada linha é uma Pessoa (Aluno, Instrutor ou Funcionario). */
public class PessoaTableModel extends AbstractTableModel {
    private static final String[] COLUNAS = {"NIP", "Nome", "Tipo", "Setor", "Situação", "Descrição"};
    private List<Pessoa> dados = new ArrayList<>();

    public void setDados(List<Pessoa> novos) { this.dados = new ArrayList<>(novos); fireTableDataChanged(); }
    public Pessoa get(int linhaDoModelo) { return dados.get(linhaDoModelo); }
    public List<Pessoa> getDados() { return dados; }

    @Override public int getRowCount() { return dados.size(); }
    @Override public int getColumnCount() { return COLUNAS.length; }
    @Override public String getColumnName(int c) { return COLUNAS[c]; }
    @Override public Class<?> getColumnClass(int c) { return c == 0 ? Integer.class : String.class; }   // ordena NIP como número
    @Override public boolean isCellEditable(int l, int c) { return false; }

    @Override public Object getValueAt(int l, int c) {
        Pessoa p = dados.get(l);
        switch (c) {
            case 0: return p.getNip();
            case 1: return p.getNome();
            case 2: return p.getTipo().substring(0, 1) + p.getTipo().substring(1).toLowerCase();
            case 3: return p.getSetor();
            case 4: return p.getSituacao().toString();
            default: return p.getDescricao();            // polimorfismo: cada subclasse responde do seu jeito
        }
    }
}
