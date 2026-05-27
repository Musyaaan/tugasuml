/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUIP5;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/**
 *
 * @author zanke
 */
public class Testkoneksi {
 // Sesuaikan dengan nama package Anda di NetBeans (lihat baris 6 di gambar Anda)
    public static void main(String[] args) {
        Connection cn = null;
        try {
            // 1. Memanggil Driver JDBC
            Class.forName("com.mysql.jdbc.Driver");
            
            // 2. Membuka Koneksi ke Database "MHS" di localhost XAMPP
            String url = "jdbc:mysql://localhost:3306/MHS";
            String user = "root";
            String password = ""; // Biarkan kosong jika password XAMPP belum pernah Anda ubah
            
            cn = DriverManager.getConnection(url, user, password);
            
            // 3. Pesan Jika Sukses
            System.out.println("=====================================");
            System.out.println("BERHASIL: Koneksi ke Database MHS Sukses!");
            System.out.println("=====================================");
            
        } catch (ClassNotFoundException e) {
            System.out.println("ERROR DRIVER: Library MySQL (JAR) belum ditambahkan ke folder Libraries!");
            System.out.println("Pesan Error: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("ERROR DATABASE: Pastikan XAMPP (MySQL) menyala dan database MHS sudah dibuat!");
            System.out.println("Pesan Error: " + e.getMessage());
        } finally {
            // Menutup koneksi (Praktek yang baik)
            try {
                if (cn != null) {
                    cn.close();
                }
            } catch (SQLException ex) {
                System.out.println("Gagal menutup koneksi.");
            }
        }
    }
}