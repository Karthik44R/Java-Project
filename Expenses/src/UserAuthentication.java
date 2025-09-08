import DataBase.DBConnection;
import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserAuthentication {

    private static String hashPassword(String password) {
    try {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(password.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            hexString.append(Integer.toHexString(0xFF & b));
        }
        return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
    }
    return null;
}

    public static boolean registerUser(String username, String password) {
        String hashedPassword = hashPassword(password);
        try (Connection con = DBConnection.getConnection()) {
            String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, hashedPassword);
            int result = ps.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            System.out.println("Error registering user: " + e.getMessage());
            return false;
        }
    }

    public static boolean loginUser(String username, String password) {
        String hashedPassword = hashPassword(password);
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, hashedPassword);
            ResultSet rs = ps.executeQuery();
            return rs.next();  // returns true if a matching user is found
        } catch (SQLException e) {
            System.out.println("Error logging in: " + e.getMessage());
            return false;
        }
    }

    public static int getUserIdByUsername(String username) {
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT id FROM users WHERE username = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
        }
        return -1;
    }
}
