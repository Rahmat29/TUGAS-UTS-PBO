/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package form;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;

/**
 * CLASS CUSTOM RENDERER (Teks Multi-baris)
 * ----------------------------------------
 * Mengubah data String (Teks panjang) agar bisa tampil
 * multi-baris (wrap text) di dalam sel JTable.
 */

/**
 *
 * @author User
 */
public class TextAreaRenderer extends JTextArea implements TableCellRenderer {

    public TextAreaRenderer() {
        setLineWrap(true);       // Aktifkan text wrapping
        setWrapStyleWord(true);  // Bungkus per kata
        setOpaque(true);
        setEditable(false);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        
        // Set teks ke JTextArea
        setText((value == null) ? "" : value.toString());
        
        // Mengatur tinggi baris JTable secara dinamis
        // agar sesuai dengan tinggi JTextArea
        int fontHeight = getFontMetrics(getFont()).getHeight();
        int textLength = getText().length();
        int lines = (textLength == 0) ? 1 : (int) (textLength / (getColumns() != 0 ? getColumns() : 1)) +1;
        
        // Kalkulasi tinggi baris (minimal 120px)
        int newHeight = Math.max(fontHeight * lines, 120); // Set tinggi minimal
        if (table.getRowHeight(row) != newHeight) {
            table.setRowHeight(row, newHeight);
        }

        // Mengatur warna background saat sel dipilih (selected)
        if (isSelected) {
            setBackground(table.getSelectionBackground());
            setForeground(table.getSelectionForeground());
        } else {
            setBackground(table.getBackground());
            setForeground(table.getForeground());
        }

        return this;
    }
}
    
