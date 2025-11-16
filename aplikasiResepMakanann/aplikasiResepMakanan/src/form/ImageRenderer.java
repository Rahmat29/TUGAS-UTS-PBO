/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package form;
import java.awt.Component;
import java.awt.Image;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * CLASS CUSTOM RENDERER
 * ---------------------
 * Class ini bertugas mengubah tampilan sel JTable.
 * Secara default, JTable hanya menampilkan teks. Class ini memaksa JTable
 * untuk menampilkan Gambar (ImageIcon) jika kolom tersebut berisi path gambar.
 */

/**
 *
 * @author User
 */
public class ImageRenderer extends DefaultTableCellRenderer {

    /**
     * Method ini dipanggil otomatis oleh JTable untuk setiap sel yang akan digambar.
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        
        // 'value' berisi string path gambar dari database
        String fotoPath = (String) value;

        // Kita gunakan JLabel sebagai wadah gambar
        JLabel label = new JLabel();
        label.setHorizontalAlignment(JLabel.CENTER); // Posisi tengah
        
        // Cek apakah path tidak null, tidak kosong, dan filenya benar-benar ada
        if (fotoPath != null && !fotoPath.isEmpty() && new File(fotoPath).exists()) {
            try {
                // 1. Load gambar asli
                ImageIcon icon = new ImageIcon(fotoPath);
                // 2. Ambil resource gambar
                Image img = icon.getImage();
                // 3. Resize gambar menjadi 80x80 pixel agar muat di tabel
                Image imgScaled = img.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                // 4. Set gambar ke label
                label.setIcon(new ImageIcon(imgScaled));
            } catch (Exception e) {
                label.setText("Error Load");
            }
        } else {
            // Jika tidak ada gambar, tampilkan teks ini
            label.setText("No Image");
        }
        
        return label; // Kembalikan komponen (JLabel) ke tabel
    }
}
