/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DB;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Administrator
 */
public class DBAccess {

    private Connection con;
    private Statement stmt;

    public DBAccess() {
        try {
            MyConnection mycon = new MyConnection();
            con = mycon.getConnection();
            stmt = con.createStatement();
        } catch (Exception ex) {
        }
    }

    // SELECT
    public ResultSet Query(String sql) {
        try {
            return stmt.executeQuery(sql);
        } catch (SQLException e) {
            System.out.println("Error query: " + e.getMessage());
        }
        return null;
    }

    // INSERT, UPDATE, DELETE
    public int Update(String sql) {
        try {
            return stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.out.println("Error query: " + e.getMessage());
        }
        return 0;
    }
}
