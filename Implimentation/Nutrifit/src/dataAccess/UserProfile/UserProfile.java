package dataAccess.UserProfile;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserProfile {

    private Connection connect() {
        // SQLite connection string
        String url = "jdbc:sqlite:Nutrifit/src/dataAccess/UserProfile/User.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
            System.out.println("Connection successful!");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Connection failed: " + e.getMessage());
        }
        return conn;
    }

    public void createNewTable() {
        // SQL statement for creating a new table
        String sql = "CREATE TABLE IF NOT EXISTS UserProfile (\n"
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + " name TEXT NOT NULL,\n"
                + " age INTEGER,\n"
                + " sex TEXT,\n"
                + " height_cm REAL,\n"
                + " weight_kg REAL\n"
                + ");";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // create a new table
            pstmt.executeUpdate();
            System.out.println("Table created successfully!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void insert(String name, int age, String sex, double height_cm, double weight_kg) {
        Connection conn = this.connect();
        if (conn != null) {
            String sql = "INSERT INTO UserProfile(name, age, sex, height_cm, weight_kg) VALUES(?,?,?,?,?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setInt(2, age);
                pstmt.setString(3, sex);
                pstmt.setDouble(4, height_cm);
                pstmt.setDouble(5, weight_kg);
                pstmt.executeUpdate();
                System.out.println("A new user profile has been inserted.");
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        } else {
            System.out.println("Connection is null. Cannot perform insert.");
        }
    }

    public void update(int id, String name, int age, String sex, double height_cm, double weight_kg) {
        String sql = "UPDATE UserProfile SET name = ?, age = ?, sex = ?, height_cm = ?, weight_kg = ? WHERE id = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, age);
            pstmt.setString(3, sex);
            pstmt.setDouble(4, height_cm);
            pstmt.setDouble(5, weight_kg);
            pstmt.setInt(6, id);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("User profile updated successfully.");
            } else {
                System.out.println("A profile with the specified ID was not found.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void deleteAll() {
        String sqlDeleteAll = "DELETE FROM UserProfile";
        String sqlResetAutoIncrement = "DELETE FROM sqlite_sequence WHERE name = 'UserProfile'";

        try (Connection conn = this.connect();
             PreparedStatement pstmtDeleteAll = conn.prepareStatement(sqlDeleteAll);
             PreparedStatement pstmtResetAutoIncrement = conn.prepareStatement(sqlResetAutoIncrement)) {

            // execute the delete statement
            int affectedRows = pstmtDeleteAll.executeUpdate();

            if (affectedRows > 0) {
                System.out.println(affectedRows + " profiles deleted successfully.");
                // Reset auto-increment counter if rows were deleted
                pstmtResetAutoIncrement.executeUpdate();
                System.out.println("Auto-increment ID reset successfully.");
            } else {
                System.out.println("The table is already empty or does not exist.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void selectAll() {
        String sql = "SELECT id, name, age, sex, height_cm, weight_kg FROM UserProfile";

        try (Connection conn = this.connect();
             PreparedStatement pstmt  = conn.prepareStatement(sql);
             ResultSet rs    = pstmt.executeQuery()) {

            // loop through the result set
            while (rs.next()) {
                System.out.println(rs.getInt("id") +  "\t" +
                        rs.getString("name") + "\t" +
                        rs.getInt("age") + "\t" +
                        rs.getString("sex") + "\t" +
                        rs.getDouble("height_cm") + "\t" +
                        rs.getDouble("weight_kg"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        UserProfile userProfile = new UserProfile();

        userProfile.createNewTable();
        userProfile.insert("James pop", 28, "Female", 165, 60);
        userProfile.update(1, "Jane Smith", 29, "Female", 165, 60);
        userProfile.insert("Mukul pop", 28, "Female", 165, 60);

        userProfile.selectAll();

        userProfile.deleteAll();
        userProfile.selectAll();
    }
}
