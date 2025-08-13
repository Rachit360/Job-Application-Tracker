package job_application_tracker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Admin {

    Connection con;
    Scanner sc;

    public Admin(Connection connection) {
        this.con = connection;
        this.sc = new Scanner(System.in);
    }

    public void menu(){
        System.out.print("Enter Admin ID: ");
        int adminId = sc.nextInt();
        System.out.print("Enter Admin Password: ");
        String adminPass = sc.next();
        if(login(adminId, adminPass)){
            System.out.println("\n\tAdmin logged in successfully!\n");
            while(true){
                System.out.println("1 - Create HR\n2 - Create User\n3 - "
                		+ "View HRs\n4 - View Users\n5 - Delete HR\n6 - "
                		+ "Delete User\n7 - Search HR/User\n0 - Logout\n");
                System.out.print("Enter your choice: ");
                int choice = sc.nextInt();
                switch(choice){
                    case 1:
                        createHR();
                        break;
                    case 2:
                        createUser();
                        break;
                    case 3:
                        viewHRs();
                        break;
                    case 4:
                        viewUsers();
                        break;
                    case 5:
                        deleteHR();
                        break;
                    case 6:
                        deleteUser();
                        break;
                    case 7:
                    	search();
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("\n\tInvalid choice. Please try again.\n");
                }
            }
        } else {
            System.out.println("\n\tInvalid Admin credentials.\n");
        }
    }

    public boolean login(int id, String password){
        try{
            String query = "SELECT * FROM admin WHERE id = ? AND password = ?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, id);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();
            return rs.next();
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void createHR(){
        System.out.print("Enter HR ID: ");
        int hrId = sc.nextInt();
        System.out.print("Enter HR Username: ");
        String hrUsername = sc.next();
        System.out.print("Enter HR Password: ");
        String hrPassword = sc.next();
        try{
            String query = "INSERT INTO hr (id, username, password) VALUES (?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, hrId);
            pst.setString(2, hrUsername);
            pst.setString(3, hrPassword);
            pst.executeUpdate();
            System.out.println("\n\tHR created successfully!\n");
        }catch(SQLException e) {
            if(e.getErrorCode() == 1062) { 
            	// This is the Duplicate entry error code for MySQL. Rather than printing the stackTrace, 
            	// this is a much better UX.
                System.out.println("\n\tHR with that ID already exists. Kindly login or create a new account.\n");
            }else {
                e.printStackTrace();
            }
        }
    }

    public void createUser(){
        System.out.print("Enter User ID: ");
        int userId = sc.nextInt();
        System.out.print("Enter Username: ");
        String username = sc.next();
        System.out.print("Enter User Email: ");
        String useremail = sc.next();
        System.out.print("Enter User Password: ");
        String password = sc.next();
        try{
            String query = "INSERT INTO user (id, username, useremail, password) VALUES (?, ?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, userId);
            pst.setString(2, username);
            pst.setString(3, useremail);
            pst.setString(4, password);
            pst.executeUpdate();
            System.out.println("\n\tUser created successfully!\n");
        }catch(SQLException e){
        	if(e.getErrorCode() == 1062) { 
                System.out.println("\n\tUser with that ID already exists. Kindly login or create a new account.\n");
            }else {
                e.printStackTrace();
            }
        }
    }

    public void viewHRs(){
        try{
            String query = "SELECT * FROM hr";
            PreparedStatement pst = con.prepareStatement(query);
            ResultSet rs = pst.executeQuery();
            System.out.println("\nList of HRs:");
            if(!rs.isBeforeFirst()) {
            	System.out.println("\tNo HR's exist!");
            }
            System.out.println();
            while(rs.next()) {
                System.out.println("\tID: " + rs.getInt("id") + ", Username: " + rs.getString("username") 
                + "\tEmail: " + rs.getString("email"));
            }
            System.out.println();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
    
    public void viewUsers(){
        try{
            String query = "SELECT * FROM user";
            PreparedStatement pst = con.prepareStatement(query);
            ResultSet rs = pst.executeQuery();
            System.out.println("\nList of Users:");
            if(!rs.isBeforeFirst()) {
            	System.out.println("\tNo Users exist!");
            }
            System.out.println();
            while(rs.next()) {
                System.out.println("\tID: " + rs.getInt("id") + ", Username: " + rs.getString("username") + ", "
                		+ "Email: " + rs.getString("useremail"));
            }
            System.out.println();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void deleteHR() {
        System.out.print("Enter HR ID to delete: ");
        int hrId = sc.nextInt();
        try{
            String query = "DELETE FROM hr WHERE id = ?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, hrId);
            int rowsAffected = pst.executeUpdate();
            if(rowsAffected > 0){
                System.out.println("\n\tHR deleted successfully!\n");
            }else{
                System.out.println("\n\tNo HR found with the given ID.\n");
            }
        }catch(SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void deleteUser(){
        System.out.print("Enter User ID to delete: ");
        int userId = sc.nextInt();
        try{
            String query = "DELETE FROM user WHERE id = ?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, userId);
            int rowsAffected = pst.executeUpdate();
            if(rowsAffected > 0){
                System.out.println("\n\tUser deleted successfully!\n");
            }else{
                System.out.println("\n\tNo User found with the given ID.\n");
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
    
    public void search() {
    	while(true) {
    		System.out.println("\n1 - HR\n2 - User\n0 - Exit");
        	System.out.print("\nEnter your choice: ");
    		int choice = sc.nextInt();
    		if(choice == 1 || choice == 2) {
    			System.out.print("Enter the id: ");
    	    	int id = sc.nextInt();
        		try{
        			String query = (choice == 1) ? 
        						(query = "SELECT * FROM hr WHERE id = ?") : 
        						(query = "SELECT * FROM user WHERE id = ?");
                    PreparedStatement pst = con.prepareStatement(query);
                    pst.setInt(1, id);
                    ResultSet rs = pst.executeQuery();
                    if(choice == 1) {
                    	 while(rs.next()) {
                         	System.out.println("\n\tid: "+rs.getInt("id")+"\n\tUserName: "+
                         rs.getString("username")+"\n\tName: "+rs.getString("name")+"\n\tEmail: "+
                         rs.getString("email")+"\n\tDate of Joining: "+
                         rs.getString("date_of_joining"));
                         }
                    	 System.out.println();
                    } else if (choice == 2) {
                    	while(rs.next()) {
                         	System.out.println("\n\tId: "+rs.getInt("id")+"\n\tUserName: "+
                         rs.getString("username")+"\n\tEmail: "+ rs.getString("useremail")+
                         "\n\tName: "+rs.getString("name")+"\n\tDegree: "+rs.getString("degree")+
                         "\n\tGraduation Year: "+rs.getInt("graduation_year")+"\n\tPecentage: "+
                         rs.getFloat("percentage"));
                         }
                    	System.out.println();
                    }
                }catch (SQLException e) {
                    e.printStackTrace();
                }
        		return;
        	} else if(choice == 0) {
        		return;
        	}else{
        		System.out.println("\n\tInavlid Input!");
        		sc.nextLine();
        	}
    	}
    	
    	
    }
}
