/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
/**
 * CLASS MODEL (Entity)
 * --------------------
 * Class ini adalah representasi OOP (Object-Oriented Programming) dari tabel 'resep'.
 * Berfungsi sebagai "wadah" untuk membawa data antar class (misal dari Database ke GUI).
 */

/**
 *
 * @author User
 */
public class Resep {

    // --- Fields / Atribut ---
    // Menggunakan access modifier 'private' untuk Encapsulation (Keamanan data)
    private int id;
    private String namaMasakan;
    private String kategori;
    private String bahan;
    private String langkah;
    private String fotoPath;

    // --- Constructor ---
    // Constructor kosong wajib ada untuk inisialisasi objek tanpa data awal
    public Resep() {
    }

    // --- Getters & Setters ---
    // Method public untuk mengambil (Get) dan mengisi (Set) nilai variabel private di atas.

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNamaMasakan() { return namaMasakan; }
    public void setNamaMasakan(String namaMasakan) { this.namaMasakan = namaMasakan; }

    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }

    public String getBahan() { return bahan; }
    public void setBahan(String bahan) { this.bahan = bahan; }

    public String getLangkah() { return langkah; }
    public void setLangkah(String langkah) { this.langkah = langkah; }

    public String getFotoPath() { return fotoPath; }
    public void setFotoPath(String fotoPath) { this.fotoPath = fotoPath; }
}