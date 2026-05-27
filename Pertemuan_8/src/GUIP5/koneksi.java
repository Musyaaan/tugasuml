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
 * @author syauqi
 */
public class koneksi {

    // Gunakan 'static' agar bisa dipanggil langsung: Koneksi.getKoneksi()
    public static Connection getKoneksi() {
        Connection cn = null;
        try {
            // Memanggil Driver MySQL sesuai Modul [cite: 1, 126]
            Class.forName("com.mysql.jdbc.Driver");
            
            // Parameter koneksi ke database MHS [cite: 1, 136]
            String url = "jdbc:mysql://localhost:3306/MHS";
            String user = "root";
            String password = ""; 
            
            cn = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Koneksi Gagal: " + e.getMessage());
        }
        return cn;
    }
}
