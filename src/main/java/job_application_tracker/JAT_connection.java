package job_application_tracker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JAT_connection {
    
	String URL = "jdbc:mysql://localhost:3306/job_portal";
    String USER = "root";
    String PASSWORD = "1234";

    public Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }
}

