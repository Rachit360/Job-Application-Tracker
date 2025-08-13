package job_application_tracker;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class User {
    public Connection con;
    public Scanner sc;

    public User(Connection connection){
        this.con = connection;
        this.sc = new Scanner(System.in);
    }

    public void menu(){
        System.out.print("Enter User ID: ");
        int userId = sc.nextInt();
        System.out.print("Enter User Password: ");
        String userPass = sc.next();

        if(login(userId, userPass)){
            System.out.println("\n\tUser logged in successfully!");
            while (true) {
                System.out.println("\n1 - View Job Openings\n2 - Apply for Job"
                		+ "\n3 - View Applied Jobs\n4 - User Data\n0 - Logout\n");
                System.out.print("Enter your choice: ");
                int choice = sc.nextInt();
                switch (choice) {
                case 1:
                    viewJobOpenings();
                    break;
                case 2:
                    applyForJob(userId);
                    break;
                case 3:
                    viewAppliedJobs(userId);
                    break;
                case 4:
                	userData(userId);
                	break;
                case 0:
                    return;
                default:
                    System.out.println("\n\tInvalid choice. Please try again.\n");
            }
            }
        }else{
            System.out.println("\n\tInvalid User credentials.\n");
        }
    }

    public boolean login(int id, String password){
        try{
            String query = "SELECT * FROM user WHERE id = ? AND password = ?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, id);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();
            if(!rs.isBeforeFirst()){
                query = "SELECT * FROM user WHERE id = ?";
                pst = con.prepareStatement(query);
                pst.setInt(1, id);
                rs = pst.executeQuery();
                if (!rs.next()) {
                    System.out.println("\n\tKindly ask admin to add your credentials.\n");
                }
            }
            return rs.next();
        } catch(SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public void viewJobOpenings() {
        try{
        	String query = "SELECT * FROM job_openings";
        	PreparedStatement pst = con.prepareStatement(query);
        	ResultSet rs = pst.executeQuery();
        	if(!rs.isBeforeFirst()) {
        		System.out.println("\n\tNo jobs posted yet!");
        	}else {
        		query = "SELECT * FROM job_openings WHERE status = 'active'";
                pst = con.prepareStatement(query);
                rs = pst.executeQuery();
                System.out.println();
                while(rs.next()){
                    System.out.println("\tJob ID: " + rs.getInt("id") + "\tTitle: "+rs.getString("title") + 
                    		"\tDescription: "+rs.getString("description") + "\tStatus: "+rs.getString("status"));
                }
        	}
            
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public void applyForJob(int userId){
        System.out.print("Enter Job ID: ");
        int jobId = sc.nextInt();

        try{
            String checkQuery = "SELECT status FROM job_openings WHERE id = ?";
            PreparedStatement checkPst = con.prepareStatement(checkQuery);
            checkPst.setInt(1, jobId);
            ResultSet checkRs = checkPst.executeQuery();

            if(checkRs.next()){
                String jobStatus = checkRs.getString("status");
                if("active".equals(jobStatus)){
                    String alreadyAppliedQuery = "SELECT * FROM applications WHERE user_id = ? AND job_id = ?";
                    PreparedStatement alreadyAppliedPst = con.prepareStatement(alreadyAppliedQuery);
                    alreadyAppliedPst.setInt(1, userId);
                    alreadyAppliedPst.setInt(2, jobId);
                    ResultSet alreadyAppliedRs = alreadyAppliedPst.executeQuery();
                    if(alreadyAppliedRs.next()){
                        System.out.println("\n\tYou have already applied for this job.");
                    } else {
                        System.out.print("Enter the file path of your resume: ");
                        sc.nextLine();
                        String resumePath = sc.nextLine();

                        File resumeFile = new File(resumePath);
                        if (!resumeFile.exists() || !resumeFile.isFile()) {
//                        	Both conditions are necessary as the first checks if the file exists while
//                        	the second checks if it is a proper file (not any system file/directories)
                            System.out.println("\n\tInvalid file path. Please try again.");
                            return;
                        }

                        String insertQuery = "INSERT INTO applications (user_id, job_id, status, resume) VALUES (?, ?, 'pending', ?)";
                        PreparedStatement insertPst = con.prepareStatement(insertQuery);
                        insertPst.setInt(1, userId);
                        insertPst.setInt(2, jobId);
                        insertPst.setString(3, resumePath);
                        insertPst.executeUpdate();
                        System.out.println("\n\tApplied for job successfully!");
                    }
                } else {
                    System.out.println("\n\tSorry, this job is no longer active.");
                }
            } else {
                System.out.println("\n\tJob ID not found.\n");
            }
        } catch(SQLException e){
            e.printStackTrace();
        }
    }


    public void viewAppliedJobs(int userId){
        try{
        	String query = "SELECT * FROM applications where user_id = ?";
        	PreparedStatement pst = con.prepareStatement(query);
        	pst.setInt(1, userId);
        	ResultSet rs = pst.executeQuery();
        	if(!rs.isBeforeFirst()) {
        		System.out.println("\n\tYou haven't applied for any jobs yet!");
        	}else {
        		query = "SELECT j.title, a.status FROM applications a JOIN job_openings j ON a.job_id = j.id "
        				+ "WHERE a.user_id = ?";
                pst = con.prepareStatement(query);
                pst.setInt(1, userId);
                rs = pst.executeQuery();
                System.out.println();
                while(rs.next()){
                    String title = rs.getString("title");
                    String status = rs.getString("status");
                    System.out.println("\tJob Title: " + title + ", Status: " + status);
                }
        	}
        } catch(SQLException e){
            e.printStackTrace();
        }
    }
    
    public void userData(int userId) {
        while (true) {
            System.out.println("1 - View User Data\n2 - Delete User Data\n0 - Back to Main Menu\n");
            System.out.print("Enter your choice: ");
            int choice = sc.nextInt();
            System.out.println();
            switch (choice) {
                case 1:
                    viewUserData(userId);
                    break;
                case 2:
                    deleteUserData(userId);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("\n\tInvalid choice. Please try again.\n");
            }
        }
    }

    public void viewUserData(int userId) {
        try {
            String query = "SELECT * FROM user WHERE id = ?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                if (rs.getString("name") == null || rs.getString("useremail") == null || rs.getString("degree") == null ||
                    rs.getInt("graduation_year") == 0 || rs.getFloat("percentage") == 0.0) {
                    System.out.println("\n\tYou haven't updated all of your details. Kindly enter them.\n");
                    updateUserData(userId);
                } else {
                    System.out.println("\n\tName: " + rs.getString("name") +
                                       "\n\tEmail: " + rs.getString("useremail") +
                                       "\n\tDegree: " + rs.getString("degree") +
                                       "\n\tGraduation Year: " + rs.getInt("graduation_year") +
                                       "\n\tPercentage: " + rs.getFloat("percentage"));
                }
            } else {
                System.out.println("\n\tUser data not found.\n");
            }
            System.out.println();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateUserData(int userId) {
        try {
            System.out.print("Enter your name: ");
            String name = sc.next();
            System.out.print("Enter your degree: ");
            String degree = sc.next();
            System.out.print("Enter your graduation year: ");
            int graduationYear = sc.nextInt();
            System.out.print("Enter your percentage: ");
            float percentage = sc.nextFloat();
            sc.nextLine();

            String query = "UPDATE user SET name = ?, degree = ?, graduation_year = ?, percentage = ? WHERE id = ?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, name);
            pst.setString(2, degree);
            pst.setInt(3, graduationYear);
            pst.setFloat(4, percentage);
            pst.setInt(5, userId);
            int rowsUpdated = pst.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("\n\tUser details updated successfully.\n");
            } else {
                System.out.println("\n\tFailed to update user details.\n");
            }
            System.out.println();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteUserData(int userId) {
        try {
            String query = "DELETE FROM user WHERE id = ?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, userId);
            pst.executeUpdate();
            System.out.println("\n\tUser data deleted successfully.\n");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
