/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package crud;
// Import Library PDF (iText 5)
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.Image; 

// Import Java IO & SQL
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import model.Resep;

/**
 * CLASS CRUD (Controller)
 * -----------------------
 * "Otak" aplikasi yang menangani manipulasi data (SQL) dan logika PDF.
 */

/**
 *
 * @author User
 */
public class crud {
    
    public Connection conn; 

    // Penanda khusus untuk data tersembunyi
    private final String START_MARKER = "###DATA_RESEP_START###";
    private final String END_MARKER = "###DATA_RESEP_END###";
    private final String SEP_COL = "##COL##"; // Pemisah kolom
    private final String SEP_ROW = "##ROW##"; // Pemisah baris
    private final String SEP_NEWLINE = "##NL##"; // Pengganti Enter/Newline

    public crud() {
        koneksi myKoneksi = new koneksi(); 
        conn = myKoneksi.getKoneksi();
    }

    private String safeCellToString(Object obj) {
        return (obj == null) ? "" : obj.toString();
    }

    // --- READ: Tampil Data ---
    public void tampilData(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT * FROM resep ORDER BY id DESC";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String path = rs.getString("foto_path");
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    path, 
                    rs.getString("nama_masakan"),
                    rs.getString("kategori"),
                    rs.getString("bahan"),
                    rs.getString("langkah"),
                    path
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Gagal Tampil: " + e.getMessage());
        }
    }

    // --- CREATE ---
    public boolean simpanData(Resep r) {
        String sql = "INSERT INTO resep (nama_masakan, kategori, bahan, langkah, foto_path) VALUES (?,?,?,?,?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, r.getNamaMasakan());
            ps.setString(2, r.getKategori());
            ps.setString(3, r.getBahan());
            ps.setString(4, r.getLangkah());
            ps.setString(5, r.getFotoPath());
            ps.executeUpdate();
            return true;
        } catch (Exception e) { return false; }
    }

    // --- UPDATE ---
    public boolean ubahData(Resep r) {
        String sql = "UPDATE resep SET nama_masakan=?, kategori=?, bahan=?, langkah=?, foto_path=? WHERE id=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, r.getNamaMasakan());
            ps.setString(2, r.getKategori());
            ps.setString(3, r.getBahan());
            ps.setString(4, r.getLangkah());
            ps.setString(5, r.getFotoPath());
            ps.setInt(6, r.getId());
            ps.executeUpdate();
            return true;
        } catch (Exception e) { return false; }
    }

    // --- DELETE ---
    public boolean hapusData(int id) {
        String sql = "DELETE FROM resep WHERE id=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (Exception e) { return false; }
    }

    // --- GET BY ID ---
    public Resep getResepById(int id) {
        String sql = "SELECT * FROM resep WHERE id=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                Resep r = new Resep();
                r.setId(rs.getInt("id"));
                r.setNamaMasakan(rs.getString("nama_masakan"));
                r.setKategori(rs.getString("kategori"));
                r.setBahan(rs.getString("bahan"));
                r.setLangkah(rs.getString("langkah"));
                r.setFotoPath(rs.getString("foto_path"));
                return r;
            }
        } catch (Exception e) {}
        return null;
    }

    // --- SEARCH ---
    public void cariData(DefaultTableModel model, String key) {
        model.setRowCount(0);
        String sql = "SELECT * FROM resep WHERE nama_masakan LIKE ? OR kategori LIKE ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%"+key+"%");
            ps.setString(2, "%"+key+"%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String path = rs.getString("foto_path");
                model.addRow(new Object[]{ 
                    rs.getInt("id"), path, rs.getString("nama_masakan"), 
                    rs.getString("kategori"), rs.getString("bahan"), 
                    rs.getString("langkah"), path 
                });
            }
        } catch (Exception e) {}
    }

    // ========================================================================
    // FITUR PDF LANJUTAN (DATA ENCODED)
    // ========================================================================

    public void exportPdf(DefaultTableModel model, File file) {
        try {
            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(file));
            doc.open();
            
            // 1. Judul Laporan
            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Laporan Lengkap Buku Resep", fontTitle);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            doc.add(title);
            doc.add(new Paragraph(" ")); // Spasi
            
            // 2. Tabel Visual (Untuk Manusia) - 5 Kolom
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 2f, 3f, 2f, 4f, 4f }); // Lebar kolom
            
            // Header
            table.addCell("Gambar"); table.addCell("Nama"); table.addCell("Kategori");
            table.addCell("Bahan"); table.addCell("Langkah");
            
            // String Builder untuk menyimpan Data Tersembunyi (Untuk Mesin/Import)
            StringBuilder rawData = new StringBuilder();
            rawData.append(START_MARKER); 
            
            for(int i=0; i<model.getRowCount(); i++){
                // --- BAGIAN TABEL VISUAL ---
                String path = safeCellToString(model.getValueAt(i, 1));
                String nama = safeCellToString(model.getValueAt(i, 2));
                String kategori = safeCellToString(model.getValueAt(i, 3));
                String bahan = safeCellToString(model.getValueAt(i, 4));
                String langkah = safeCellToString(model.getValueAt(i, 5));
                String pathReal = safeCellToString(model.getValueAt(i, 6)); // Path asli di kolom hidden

                // Kolom Gambar
                PdfPCell imageCell = new PdfPCell();
                if (!path.isEmpty() && new File(path).exists()) {
                    try {
                        Image img = Image.getInstance(path);
                        img.scaleToFit(60, 60); 
                        imageCell.addElement(img);
                    } catch (Exception ex) { imageCell.addElement(new Paragraph("Img Err")); }
                } else {
                    imageCell.addElement(new Paragraph("No Img"));
                }
                table.addCell(imageCell);
                
                // Kolom Teks
                table.addCell(nama);
                table.addCell(kategori);
                table.addCell(bahan);
                table.addCell(langkah);
                
                // --- BAGIAN DATA TERSEMBUNYI (SERIALIZATION) ---
                // Kita ganti karakter 'enter' (\n) dengan penanda khusus agar tidak merusak format baris
                String safeBahan = bahan.replace("\n", SEP_NEWLINE);
                String safeLangkah = langkah.replace("\n", SEP_NEWLINE);
                
                // Format: Nama #COL# Kategori #COL# Bahan #COL# Langkah #COL# Path #ROW#
                rawData.append(nama).append(SEP_COL)
                       .append(kategori).append(SEP_COL)
                       .append(safeBahan).append(SEP_COL)
                       .append(safeLangkah).append(SEP_COL)
                       .append(pathReal).append(SEP_ROW);
            }
            
            doc.add(table);
            
            // 3. Menulis Data Tersembunyi (Sangat Kecil / Putih)
            // Ini trik agar kita bisa import balik datanya dengan akurat 100%
            rawData.append(END_MARKER);
            Font fontHidden = FontFactory.getFont(FontFactory.COURIER, 1, BaseColor.WHITE); // Font ukuran 1, warna putih (tak terlihat)
            doc.add(new Paragraph(rawData.toString(), fontHidden));
            
            doc.close();
            JOptionPane.showMessageDialog(null, "Export Berhasil! Data tersimpan lengkap di PDF.");
            
        } catch (Exception e) { 
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Gagal Export: " + e.getMessage()); 
        }
    }
    
    public void importPdf(File file) {
        try {
            PdfReader reader = new PdfReader(new FileInputStream(file));
            int pages = reader.getNumberOfPages();
            StringBuilder fullText = new StringBuilder();
            
            // Baca semua halaman PDF
            for(int i=1; i<=pages; i++) {
                fullText.append(PdfTextExtractor.getTextFromPage(reader, i));
            }
            
            String content = fullText.toString();
            
            // Cari blok data tersembunyi
            if (content.contains(START_MARKER) && content.contains(END_MARKER)) {
                // Ambil teks di antara MARKER START dan END
                String dataBlock = content.substring(
                        content.indexOf(START_MARKER) + START_MARKER.length(), 
                        content.indexOf(END_MARKER)
                );
                
                // Pecah menjadi array per Resep (Baris)
                String[] rows = dataBlock.split(SEP_ROW);
                int count = 0;
                
                for (String row : rows) {
                    // Pecah menjadi kolom
                    String[] cols = row.split(SEP_COL);
                    
                    // Pastikan ada 5 kolom data (Nama, Kategori, Bahan, Langkah, Path)
                    if (cols.length >= 5) {
                        Resep r = new Resep();
                        r.setNamaMasakan(cols[0]);
                        r.setKategori(cols[1]);
                        // Kembalikan Newline
                        r.setBahan(cols[2].replace(SEP_NEWLINE, "\n"));
                        r.setLangkah(cols[3].replace(SEP_NEWLINE, "\n"));
                        r.setFotoPath(cols[4]);
                        
                        if(simpanData(r)) count++;
                    }
                }
                JOptionPane.showMessageDialog(null, "Berhasil Import " + count + " Resep Lengkap!");
            } else {
                JOptionPane.showMessageDialog(null, "Format PDF tidak dikenali atau bukan hasil Export aplikasi ini.");
            }
            
            reader.close();
        } catch (Exception e) { 
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Gagal Import: " + e.getMessage()); 
        }
    }
}