import DataBase.DBConnection;
import java.sql.*;
import java.util.Scanner;
import java.text.SimpleDateFormat;

public class ExpenseTracker {
    private static Scanner sc = new Scanner(System.in);
    private static int loggedInUserId = -1;

    public static void main(String[] args) {
        while (true) {
            if (loggedInUserId == -1) {
                System.out.println("\n Personal Finance Tracker ");
                System.out.println("1. Register");
                System.out.println("2. Login");
                System.out.println("3. Exit");
                System.out.print("Choose option: ");
                String choice = sc.nextLine();

                switch (choice) {
                    case "1": register(); break;
                    case "2": login(); break;
                    case "3": System.out.println("Thank You For Using...."); return;
                    default: System.out.println("Invalid choice!");
                }
            } else {
                System.out.println("\n Personal Finance Tracker ");
                System.out.println("1. Manage Expenses");
                System.out.println("2. Manage Income");
                System.out.println("3. Show Balance");
                System.out.println("4. Logout");
                System.out.print("Choose option: ");
                String choice = sc.nextLine();

                switch (choice) {
                    case "1": expenseMenu(); break;
                    case "2": incomeMenu(); break;
                    case "3": showBalance(); break;
                    case "4": logout(); break;
                    default: System.out.println("Invalid choice!");
                }
            }
        }
    }

    private static void register() {
        System.out.print("Enter username: ");
        String username = sc.nextLine();
        System.out.print("Enter password: ");
        String password = sc.nextLine();

        if (UserAuthentication.registerUser(username, password)) {
            System.out.println("Account created successfully!");
        } else {
            System.out.println("Account creation failed Try again!");
        }
    }

    private static void login() {
        System.out.print("Enter username: ");
        String username = sc.nextLine();
        System.out.print("Enter password: ");
        String password = sc.nextLine();

        if (UserAuthentication.loginUser(username, password)) {
            System.out.println("Login successful!");
            loggedInUserId = UserAuthentication.getUserIdByUsername(username);
        } else {
            System.out.println("Invalid username or password");
        }
    }

    private static void logout() {
        loggedInUserId = -1;
        System.out.println("You have logged out");
    }

    private static void expenseMenu() {
        while (true) {
            System.out.println("\n Expense Menu ");
            System.out.println("1. Add Expense");
            System.out.println("2. View Expenses");
            System.out.println("3. Delete Expense");
            System.out.println("4. Show Total Expenses");
            System.out.println("5. Back");
            System.out.print("Choose option: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1": addRecord("expenses"); break;
                case "2": viewRecords("expenses"); break;
                case "3": deleteRecord("expenses"); break;
                case "4": totalRecords("expenses"); break;
                case "5": return;
                default: System.out.println("Invalid choice!");
            }
        }
    }

    private static void incomeMenu() {
        while (true) {
            System.out.println("\n Income Menu ");
            System.out.println("1. Add Income");
            System.out.println("2. View Income");
            System.out.println("3. Delete Income");
            System.out.println("4. Show Total Income");
            System.out.println("5. Back");
            System.out.print("Choose option: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1": addRecord("income"); break;
                case "2": viewRecords("income"); break;
                case "3": deleteRecord("income"); break;
                case "4": totalRecords("income"); break;
                case "5": return;
                default: System.out.println("Invalid choice!");
            }
        }
    }

    private static void addRecord(String table) {
        try (Connection con = DBConnection.getConnection()) {
            System.out.print("Enter date (dd-mm-yyyy): ");
            String dateInput = sc.nextLine().trim();
            dateInput = dateInput.replace("/", "-");
            java.util.Date utilDate = new SimpleDateFormat("dd-MM-yyyy").parse(dateInput);
            java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

            System.out.print("Enter description: ");
            String desc = sc.nextLine();
            System.out.print("Enter amount: ");
            double amount = Double.parseDouble(sc.nextLine());

            String dateColumn = table.equals("expenses") ? "exp_date" : "inc_date";
            String sql = "INSERT INTO " + table + " (" + dateColumn + ", description, amount, user_id) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setDate(1, sqlDate);
            ps.setString(2, desc);
            ps.setDouble(3, amount);
            ps.setInt(4, loggedInUserId);
            ps.executeUpdate();

            System.out.println((table.equals("expenses") ? "Expense" : "Income") + " added successfully!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void viewRecords(String table) {
        try (Connection con = DBConnection.getConnection()) {
            String dateColumn = table.equals("expenses") ? "exp_date" : "inc_date";
            String sql = "SELECT * FROM " + table + " WHERE user_id = ? ORDER BY " + dateColumn + " DESC";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, loggedInUserId);
            ResultSet rs = ps.executeQuery();

            System.out.printf("%-5s %-12s %-20s %-10s\n", "ID", "Date", "Description", "Amount");
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

            while (rs.next()) {
                java.sql.Date sqlDate = rs.getDate(dateColumn);
                String formattedDate = (sqlDate != null) ? sdf.format(sqlDate) : "";
                System.out.printf("%-5d %-12s %-20s %-10.2f\n",
                        rs.getInt("id"),
                        formattedDate,
                        rs.getString("description"),
                        rs.getDouble("amount"));
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void deleteRecord(String table) {
        try (Connection con = DBConnection.getConnection()) {
            System.out.print("Enter ID to delete: ");
            int id = Integer.parseInt(sc.nextLine());
            String sql = "DELETE FROM " + table + " WHERE id = ? AND user_id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            ps.setInt(2, loggedInUserId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println((table.equals("expenses") ? "Expense" : "Income") + " deleted successfully");
            } else {
                System.out.println("Record not found or you don't have permission to delete");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void totalRecords(String table) {
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT SUM(amount) FROM " + table + " WHERE user_id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, loggedInUserId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double total = rs.getDouble(1);
                System.out.printf("Total %s: %.2f\n", table, total);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void showBalance() {
        try (Connection con = DBConnection.getConnection()) {
            String sqlIncome = "SELECT SUM(amount) FROM income WHERE user_id = ?";
            PreparedStatement psInc = con.prepareStatement(sqlIncome);
            psInc.setInt(1, loggedInUserId);
            ResultSet rsInc = psInc.executeQuery();
            double totalIncome = 0;
            if (rsInc.next()) totalIncome = rsInc.getDouble(1);

            String sqlExp = "SELECT SUM(amount) FROM expenses WHERE user_id = ?";
            PreparedStatement psExp = con.prepareStatement(sqlExp);
            psExp.setInt(1, loggedInUserId);
            ResultSet rsExp = psExp.executeQuery();
            double totalExpenses = 0;
            if (rsExp.next()) totalExpenses = rsExp.getDouble(1);

            double balance = totalIncome - totalExpenses;
            System.out.printf("Income = %.2f\nExpenses = %.2f\nBalance = %.2f\n",
                    totalIncome, totalExpenses, balance);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
