/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DB;

import java.sql.Connection;
import java.sql.DriverManager;
import javax.swing.JOptionPane;

/**
 *
 * @author Administrator
 */
public class MyConnection {

    public Connection getConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String URL = "jdbc:mysql://pma.tdsof.dev/laptrinhmang?useUnicode=true&characterEncoding=utf-8";
//            System.out.println("Kết nối MySql thành công.");
            return DriverManager.getConnection(URL, "ltm", "ltm");
        } catch (Exception ex) {
            System.out.println("Kết nối MySql thất bại.");
            JOptionPane.showMessageDialog(null, ex.toString(), "Loi", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}
