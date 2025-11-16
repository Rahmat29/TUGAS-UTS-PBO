/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package form;

// --- Import Library ---
import crud.crud;
import model.Resep;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * CLASS VIEW (GUI)
 * -----------------
 * Mengontrol tampilan GUI, event tombol, dan menghubungkan ke logika 'crud.java'.
 */
public class FrameApp extends javax.swing.JFrame {

    // --- Variabel Global ---
    crud mycode;
    DefaultTableModel model;
    CardLayout cardLayout;
    int idBaris = 0; // 0 = Mode Tambah, >0 = Mode Edit
    
    // Warna UI
    Color placeholderColor = new Color(153,153,153);
    Color activeColor = new Color(0,0,0);

    /**
     * Constructor
     */
    public FrameApp() {
        initComponents();
        initCustomLogic();
    }

    /**
     * Inisialisasi Logika Tambahan
     */
    private void initCustomLogic() {
        // 1. Siapkan Koneksi & Logika
        mycode = new crud();
        if (mycode.conn == null) {
             JOptionPane.showMessageDialog(this, "Koneksi Database Gagal!", "Error", JOptionPane.ERROR_MESSAGE);
             System.exit(0);
        }
        
        // 2. Setup Tampilan
        cardLayout = (CardLayout)(mainContent.getLayout());
        setupTabel();
        mycode.tampilData(model);
        addPlaceholder(txtCari, "Cari Resep...");
        cardLayout.show(mainContent, "cardList");

        // ==========================================
        // EVENT LISTENERS (Interaksi)
        // ==========================================

        // --- PDF Export (Tombol dari XML) ---
        btnExport.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Simpan PDF");
            chooser.setSelectedFile(new File("resep.pdf"));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".pdf")) {
                    file = new File(file.getParentFile(), file.getName() + ".pdf");
                }
                mycode.exportPdf(model, file);
            }
        });

        // --- PDF Import (Tombol dari XML) ---
        btnImport.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Fitur Import PDF membaca tabel sederhana.", "Info", JOptionPane.INFORMATION_MESSAGE);
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("PDF Documents", "pdf"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                mycode.importPdf(chooser.getSelectedFile());
                mycode.tampilData(model);
            }
        });

        // --- Navigasi Sidebar ---
        btnMenuList.addActionListener(e -> {
            cardLayout.show(mainContent, "cardList");
            mycode.tampilData(model);
        });

        // --- Tombol Tambah ---
        btnTambahBaru.addActionListener(e -> {
            resetForm();
            cardLayout.show(mainContent, "cardForm");
        });

        // --- Tombol Batal ---
        btnBatal.addActionListener(e -> {
            resetForm();
            cardLayout.show(mainContent, "cardList");
        });

        // --- Simpan Data (Create / Update) ---
        btnSimpan.addActionListener(e -> {
            if (!validasiInput()) return;
            
            Resep r = new Resep();
            r.setNamaMasakan(txtNamaMasakan.getText());
            r.setKategori(cmbKategori.getSelectedItem().toString());
            r.setBahan(areaBahan.getText());
            r.setLangkah(areaLangkah.getText());
            r.setFotoPath(lblPathGambar.getText());
            
            boolean sukses;
            if (idBaris == 0) {
                sukses = mycode.simpanData(r);
                if(sukses) JOptionPane.showMessageDialog(this, "Berhasil Disimpan!");
            } else {
                r.setId(idBaris);
                sukses = mycode.ubahData(r);
                if(sukses) JOptionPane.showMessageDialog(this, "Berhasil Diubah!");
            }
            
            if(sukses){
                mycode.tampilData(model);
                resetForm();
                cardLayout.show(mainContent, "cardList");
            }
        });

        // --- Edit Data (Double Click) ---
        tblResep.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() == 2) {
                    int baris = tblResep.getSelectedRow();
                    if (baris == -1) return;
                    
                    int id = (int) model.getValueAt(baris, 0); // ID di kolom 0
                    Resep r = mycode.getResepById(id);
                    
                    if (r != null) {
                        idBaris = id; // Mode Edit
                        lblFormTitle.setText("EDIT RESEP");
                        txtNamaMasakan.setText(r.getNamaMasakan());
                        cmbKategori.setSelectedItem(r.getKategori());
                        areaBahan.setText(r.getBahan());
                        areaLangkah.setText(r.getLangkah());
                        lblPathGambar.setText(r.getFotoPath());
                        tampilkanPreviewGambar(r.getFotoPath());
                        cardLayout.show(mainContent, "cardForm");
                    }
                }
            }
        });

        // --- Hapus Data ---
        btnHapus.addActionListener(e -> {
            int baris = tblResep.getSelectedRow();
            if (baris == -1) {
                JOptionPane.showMessageDialog(this, "Pilih resep dulu!");
                return;
            }
            if (JOptionPane.showConfirmDialog(this, "Yakin hapus?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                int id = (int) model.getValueAt(baris, 0);
                if (mycode.hapusData(id)) mycode.tampilData(model);
            }
        });

        // --- Upload Gambar ---
        btnUpload.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Gambar", "jpg", "png", "jpeg"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                String path = chooser.getSelectedFile().getAbsolutePath();
                lblPathGambar.setText(path);
                tampilkanPreviewGambar(path);
            }
        });

        // --- Cari Data ---
        txtCari.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                String key = txtCari.getText().equals("Cari Resep...") ? "" : txtCari.getText();
                mycode.cariData(model, key);
            }
        });
    }

    // ==========================================
    // METHOD HELPER (Fungsi Pembantu)
    // ==========================================

    /**
     * Setup Model JTable (tblResep)
     * --- DIPERBARUI ---
     */
    private void setupTabel() {
        // 7 Kolom: ID, Gambar, Nama, Kategori, Bahan, Langkah, Path
        String[] col = {"ID", "Gambar", "Nama Masakan", "Kategori", "Bahan", "Langkah", "Foto Path"};
        
        model = new DefaultTableModel(col, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblResep.setModel(model);
        
        // Mengatur tinggi baris menjadi 120px agar gambar dan teks muat
        tblResep.setRowHeight(120); 
        
        // Terapkan Renderer Gambar ke Kolom 1
        tblResep.getColumnModel().getColumn(1).setCellRenderer(new ImageRenderer());
        tblResep.getColumnModel().getColumn(1).setPreferredWidth(90);

        // Terapkan Renderer Teks (Multi-baris) ke Kolom 4 (Bahan)
        tblResep.getColumnModel().getColumn(4).setCellRenderer(new TextAreaRenderer());
        tblResep.getColumnModel().getColumn(4).setPreferredWidth(200);

        // Terapkan Renderer Teks (Multi-baris) ke Kolom 5 (Langkah)
        tblResep.getColumnModel().getColumn(5).setCellRenderer(new TextAreaRenderer());
        tblResep.getColumnModel().getColumn(5).setPreferredWidth(200);

        // Sembunyikan Kolom ID (0) dan Path (6)
        tblResep.getColumnModel().getColumn(0).setMinWidth(0);
        tblResep.getColumnModel().getColumn(0).setMaxWidth(0);
        tblResep.getColumnModel().getColumn(6).setMinWidth(0);
        tblResep.getColumnModel().getColumn(6).setMaxWidth(0);
    }

    /**
     * Membersihkan Form Input
     */
    private void resetForm() {
        idBaris = 0;
        lblFormTitle.setText("INPUT RESEP BARU");
        txtNamaMasakan.setText("");
        cmbKategori.setSelectedIndex(0);
        areaBahan.setText("");
        areaLangkah.setText("");
        lblPathGambar.setText("");
        lblPreviewFoto.setIcon(null);
        lblPreviewFoto.setText("No Image");
    }

    /**
     * Tampilkan Preview Gambar di Form
     */
    private void tampilkanPreviewGambar(String path) {
        if (path == null || path.isEmpty() || !new File(path).exists()) {
            lblPreviewFoto.setIcon(null);
            lblPreviewFoto.setText("No Image");
            return;
        }
        try {
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            lblPreviewFoto.setIcon(new ImageIcon(img));
            lblPreviewFoto.setText("");
        } catch (Exception e) { lblPreviewFoto.setText("Error"); }
    }

    /**
     * Validasi Input
     */
    private boolean validasiInput() {
        if (txtNamaMasakan.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama Masakan harus diisi!");
            return false;
        }
         if (areaBahan.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bahan harus diisi!");
            return false;
        }
        return true;
    }

    /**
     * Efek Placeholder (Teks abu-abu)
     */
    private void addPlaceholder(JTextField txt, String ph) {
        if(txt.getText().isEmpty()) { txt.setText(ph); txt.setForeground(placeholderColor); }
        txt.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                if (txt.getText().equals(ph)) { txt.setText(""); txt.setForeground(activeColor); }
            }
            public void focusLost(FocusEvent e) {
                if (txt.getText().isEmpty()) { txt.setText(ph); txt.setForeground(placeholderColor); }
            }
        });
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        sidebarPanel = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        btnMenuList = new javax.swing.JButton();
        mainContent = new javax.swing.JPanel();
        cardList = new javax.swing.JPanel();
        headerPanel = new javax.swing.JPanel();
        txtCari = new javax.swing.JTextField();
        btnTambahBaru = new javax.swing.JButton();
        btnExport = new javax.swing.JButton();
        btnImport = new javax.swing.JButton();
        btnHapus = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblResep = new javax.swing.JTable();
        cardForm = new javax.swing.JPanel();
        formContainer = new javax.swing.JPanel();
        lblFormTitle = new javax.swing.JLabel();
        lblNama = new javax.swing.JLabel();
        txtNamaMasakan = new javax.swing.JTextField();
        lblKategori = new javax.swing.JLabel();
        cmbKategori = new javax.swing.JComboBox();
        lblBahan = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        areaBahan = new javax.swing.JTextArea();
        lblLangkah = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        areaLangkah = new javax.swing.JTextArea();
        lblFoto = new javax.swing.JLabel();
        fotoContainer = new javax.swing.JPanel();
        lblPreviewFoto = new javax.swing.JLabel();
        btnUpload = new javax.swing.JButton();
        lblPathGambar = new javax.swing.JLabel();
        buttonPanel = new javax.swing.JPanel();
        btnSimpan = new javax.swing.JButton();
        btnBatal = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Aplikasi Buku Resep (Lengkap)");

        sidebarPanel.setBackground(new java.awt.Color(45, 52, 54));
        sidebarPanel.setPreferredSize(new java.awt.Dimension(200, 600));
        sidebarPanel.setLayout(new javax.swing.BoxLayout(sidebarPanel, javax.swing.BoxLayout.Y_AXIS));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblTitle.setForeground(new java.awt.Color(255, 255, 255));
        lblTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitle.setText("BUKU RESEP");
        lblTitle.setMaximumSize(new java.awt.Dimension(32767, 80));
        lblTitle.setPreferredSize(new java.awt.Dimension(200, 80));
        sidebarPanel.add(lblTitle);

        btnMenuList.setBackground(new java.awt.Color(45, 52, 54));
        btnMenuList.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnMenuList.setForeground(new java.awt.Color(255, 255, 255));
        btnMenuList.setText("Daftar Resep");
        btnMenuList.setMaximumSize(new java.awt.Dimension(32767, 50));
        btnMenuList.setBorderPainted(false);
        btnMenuList.setFocusPainted(false);
        btnMenuList.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnMenuList.setMargin(new java.awt.Insets(0, 20, 0, 20));
        sidebarPanel.add(btnMenuList);

        getContentPane().add(sidebarPanel, java.awt.BorderLayout.WEST);

        mainContent.setLayout(new java.awt.CardLayout());

        cardList.setBackground(new java.awt.Color(255, 255, 255));
        cardList.setLayout(new java.awt.BorderLayout());

        headerPanel.setBackground(new java.awt.Color(255, 255, 255));
        headerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        headerPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));

        txtCari.setText("Cari Resep...");
        txtCari.setPreferredSize(new java.awt.Dimension(200, 35));
        headerPanel.add(txtCari);

        btnTambahBaru.setBackground(new java.awt.Color(225, 112, 85));
        btnTambahBaru.setForeground(new java.awt.Color(255, 255, 255));
        btnTambahBaru.setText("+ Baru");
        btnTambahBaru.setPreferredSize(new java.awt.Dimension(80, 35));
        headerPanel.add(btnTambahBaru);

        btnExport.setBackground(new java.awt.Color(255, 153, 51));
        btnExport.setForeground(new java.awt.Color(255, 255, 255));
        btnExport.setText("Exp PDF");
        btnExport.setPreferredSize(new java.awt.Dimension(90, 35));
        headerPanel.add(btnExport);

        btnImport.setBackground(new java.awt.Color(204, 204, 51));
        btnImport.setForeground(new java.awt.Color(0, 0, 0));
        btnImport.setText("Imp PDF");
        btnImport.setPreferredSize(new java.awt.Dimension(90, 35));
        headerPanel.add(btnImport);

        btnHapus.setBackground(new java.awt.Color(214, 59, 59));
        btnHapus.setForeground(new java.awt.Color(255, 255, 255));
        btnHapus.setText("Hapus");
        btnHapus.setPreferredSize(new java.awt.Dimension(80, 35));
        headerPanel.add(btnHapus);

        cardList.add(headerPanel, java.awt.BorderLayout.NORTH);

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 20, 20, 20));

        tblResep.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Gambar", "Nama Masakan", "Kategori", "Bahan", "Langkah", "Foto Path"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblResep.setRowHeight(120);
        tblResep.setShowHorizontalLines(true);
        jScrollPane1.setViewportView(tblResep);
        if (tblResep.getColumnModel().getColumnCount() > 0) {
            tblResep.getColumnModel().getColumn(0).setMinWidth(0);
            tblResep.getColumnModel().getColumn(0).setPreferredWidth(0);
            tblResep.getColumnModel().getColumn(0).setMaxWidth(0);
            tblResep.getColumnModel().getColumn(1).setMinWidth(90);
            tblResep.getColumnModel().getColumn(1).setPreferredWidth(90);
            tblResep.getColumnModel().getColumn(1).setMaxWidth(100);
            tblResep.getColumnModel().getColumn(2).setPreferredWidth(150);
            tblResep.getColumnModel().getColumn(3).setPreferredWidth(100);
            tblResep.getColumnModel().getColumn(4).setPreferredWidth(200);
            tblResep.getColumnModel().getColumn(5).setPreferredWidth(200);
            tblResep.getColumnModel().getColumn(6).setMinWidth(0);
            tblResep.getColumnModel().getColumn(6).setPreferredWidth(0);
            tblResep.getColumnModel().getColumn(6).setMaxWidth(0);
        }

        cardList.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        mainContent.add(cardList, "cardList");

        cardForm.setBackground(new java.awt.Color(245, 245, 245));
        cardForm.setLayout(new java.awt.GridBagLayout());

        formContainer.setBackground(new java.awt.Color(255, 255, 255));
        formContainer.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        formContainer.setLayout(new java.awt.GridBagLayout());

        lblFormTitle.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblFormTitle.setForeground(new java.awt.Color(225, 112, 85));
        lblFormTitle.setText("INPUT RESEP BARU");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(20, 20, 20, 20);
        formContainer.add(lblFormTitle, gridBagConstraints);

        lblNama.setText("Nama Masakan:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 5, 10);
        formContainer.add(lblNama, gridBagConstraints);

        txtNamaMasakan.setPreferredSize(new java.awt.Dimension(250, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 20);
        formContainer.add(txtNamaMasakan, gridBagConstraints);

        lblKategori.setText("Kategori:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 5, 10);
        formContainer.add(lblKategori, gridBagConstraints);

        cmbKategori.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Main Course", "Dessert", "Appetizer", "Drink" }));
        cmbKategori.setPreferredSize(new java.awt.Dimension(250, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 20);
        formContainer.add(cmbKategori, gridBagConstraints);

        lblBahan.setText("Bahan-Bahan:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 5, 10);
        formContainer.add(lblBahan, gridBagConstraints);

        jScrollPane3.setPreferredSize(new java.awt.Dimension(250, 80));

        areaBahan.setColumns(20);
        areaBahan.setRows(5);
        jScrollPane3.setViewportView(areaBahan);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 20);
        formContainer.add(jScrollPane3, gridBagConstraints);

        lblLangkah.setText("Cara Memasak:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 5, 10);
        formContainer.add(lblLangkah, gridBagConstraints);

        jScrollPane4.setPreferredSize(new java.awt.Dimension(250, 80));

        areaLangkah.setColumns(20);
        areaLangkah.setRows(5);
        jScrollPane4.setViewportView(areaLangkah);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 20);
        formContainer.add(jScrollPane4, gridBagConstraints);

        lblFoto.setText("Foto:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 5, 10);
        formContainer.add(lblFoto, gridBagConstraints);

        fotoContainer.setBackground(new java.awt.Color(255, 255, 255));
        fotoContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblPreviewFoto.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblPreviewFoto.setText("No Image");
        lblPreviewFoto.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        lblPreviewFoto.setPreferredSize(new java.awt.Dimension(100, 100));
        fotoContainer.add(lblPreviewFoto);

        btnUpload.setText("Upload");
        fotoContainer.add(btnUpload);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 20);
        formContainer.add(fotoContainer, gridBagConstraints);

        lblPathGambar.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        lblPathGambar.setForeground(new java.awt.Color(153, 153, 153));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 20);
        formContainer.add(lblPathGambar, gridBagConstraints);

        buttonPanel.setBackground(new java.awt.Color(255, 255, 255));

        btnSimpan.setBackground(new java.awt.Color(225, 112, 85));
        btnSimpan.setForeground(new java.awt.Color(255, 255, 255));
        btnSimpan.setText("Simpan Resep");
        btnSimpan.setPreferredSize(new java.awt.Dimension(120, 35));
        buttonPanel.add(btnSimpan);

        btnBatal.setBackground(new java.awt.Color(153, 153, 153));
        btnBatal.setForeground(new java.awt.Color(255, 255, 255));
        btnBatal.setText("Batal");
        btnBatal.setPreferredSize(new java.awt.Dimension(100, 35));
        buttonPanel.add(btnBatal);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 20, 0);
        formContainer.add(buttonPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        cardForm.add(formContainer, gridBagConstraints);

        mainContent.add(cardForm, "cardForm");

        getContentPane().add(mainContent, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FrameApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrameApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrameApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrameApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FrameApp().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea areaBahan;
    private javax.swing.JTextArea areaLangkah;
    private javax.swing.JButton btnBatal;
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnImport;
    private javax.swing.JButton btnMenuList;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JButton btnTambahBaru;
    private javax.swing.JButton btnUpload;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JPanel cardForm;
    private javax.swing.JPanel cardList;
    private javax.swing.JComboBox cmbKategori;
    private javax.swing.JPanel formContainer;
    private javax.swing.JPanel fotoContainer;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JLabel lblBahan;
    private javax.swing.JLabel lblFormTitle;
    private javax.swing.JLabel lblFoto;
    private javax.swing.JLabel lblKategori;
    private javax.swing.JLabel lblLangkah;
    private javax.swing.JLabel lblNama;
    private javax.swing.JLabel lblPathGambar;
    private javax.swing.JLabel lblPreviewFoto;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel mainContent;
    private javax.swing.JPanel sidebarPanel;
    private javax.swing.JTable tblResep;
    private javax.swing.JTextField txtCari;
    private javax.swing.JTextField txtNamaMasakan;
    // End of variables declaration//GEN-END:variables
}