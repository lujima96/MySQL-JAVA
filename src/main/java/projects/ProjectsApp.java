package projects;

import java.sql.Connection;
import projects.dao.DbConnection;
import projects.exception.DbException;

public class ProjectsApp {
    public static void main(String[] args) {
        // Establish connection using DbConnection
        try (Connection connection = DbConnection.getConnection()) {
            // If connection is successful, print a message
            System.out.println("Connected to the database successfully.");
        } catch (DbException e) {
            System.err.println("Database error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }
}
