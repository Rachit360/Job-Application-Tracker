package job_application_tracker;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
	
    public static void main(String[] args) throws SQLException{
    	
        JAT_connection jatCon = new JAT_connection();
        Connection connection = jatCon.getConnection();
        Scanner sc = new Scanner(System.in);
        
        if (connection != null) {
            System.out.println("Connected to the database!\n");
            while (true) {
                try {
                    System.out.print("Enter login type (1-Admin, 2-HR, 3-User, 0-Exit): ");
                    int choice = sc.nextInt();
                    switch (choice) {
                        case 1:
                            Admin admin = new Admin(connection);
                            admin.menu();
                            break;
                        case 2:
                            HR hr = new HR(connection);
                            hr.menu();
                            break;
                        case 3:
                            User user = new User(connection);
                            user.menu();
                            break;
                        case 0:
                        	connection.close();
                            System.out.println("\n\tApp closed!\n");
                            return;
                    }
                } catch (InputMismatchException e) {
                    System.out.println("\n\tInvalid input. Please enter a valid integer.\n");
                    sc.nextLine();
                    //	New learning: By calling sc.nextLine(), we ensure that any 
                    //  leftover input (including the invalid one) is cleared, 
                    //  allowing the user to enter a fresh input. If not, the newline
                    //	character (enter button that we press after giving input) 
                    //  will be consumed by the nextInt and the invalid input will still
                    //  remain for every iteration causing an infinite loop.
                }
            }
        } else {
            System.out.println("\n\tFailed to connect to the database.\n");
        }
        
        sc.close();
    }
}
