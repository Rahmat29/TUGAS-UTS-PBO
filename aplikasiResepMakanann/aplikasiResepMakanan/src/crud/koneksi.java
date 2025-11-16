/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package crud;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import javax.swing.JOptionPane;

/**
 * CLASS KONEKSI
 * --------------
 * Menangani hubungan teknis antara Java dan MySQL Server.
 */

/**
 *
 * @author User
 */
public class koneksi {
    
    // Konfigurasi Database
    String url = "jdbc:mysql://localhost:3306/db_resep"; // Nama DB tujuan
    String user = "root"; // Default user XAMPP
    String pass = "";     // Default password XAMPP
    Connection Koneksidb;

    /**
     * Constructor: Dijalankan saat 'new koneksi()' dipanggil.
     */
    public koneksi() {
        try {
            // Load Driver MySQL (Gaya Legacy JDBC)
            Driver dbdriver = new com.mysql.jdbc.Driver();
            DriverManager.registerDriver(dbdriver);
            
            // Buka koneksi
            Koneksidb = DriverManager.getConnection(url, user, pass);
            System.out.println("Koneksi Berhasil");
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Koneksi Gagal: " + e.toString());
        }
    }

    // Getter untuk diambil oleh class lain
    public Connection getKoneksi() { return Koneksidb; }
}