package job_application_tracker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class HR {
	
    public Connection con;
    public Scanner sc;

    public HR(Connection connection){
        this.con = connection;
        this.sc = new Scanner(System.in);
    }

    public void menu(){
        System.out.print("Enter HR ID: ");
        int hrId = sc.nextInt();
        System.out.print("Enter HR Password: ");
        String hrPass = sc.next();
        if(login(hrId, hrPass)){
            System.out.println("\n\tHR logged in successfully!\n");
            while(true) {
                System.out.println("1 - Post Job Opening\n2 - View Job Openings\n3 - Update Job Opening\n"
                		+ "4 - Delete Job Opening\n5 - Update Applicant Status\n6 - Update Job Posting Status\n"
                		+ "7 - HR Data\n0 - Logout\n");
                System.out.print("Enter your choice: ");
                int choice = sc.nextInt();
                System.out.println();
                switch (choice) {
	                case 1:
	                    postJobOpening();
	                    break;
	                case 2:
	                    viewJobOpenings();
	                    break;
	                case 3:
	                    updateJobOpening();
	                    break;
	                case 4:
	                    deleteJobOpening();
	                    break;
	                case 5:
	                    updateApplicantStatus();
	                    break;
	                case 6:
	                    updateJobPostingStatus();
	                    break;
	                case 7:
	                    HRdata(hrId);
	                    break;
	                case 0:
	                    return;
	                default:
	                    System.out.println("\n\tInvalid choice. Please try again.\n");
                }
            }
        } else {
            System.out.println("\n\tInvalid HR credentials.\n");
        }
    }

    public boolean login(int id, String password){
        try{
            String query = "SELECT * FROM hr WHERE id = ? AND password = ?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, id);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();
            if(!rs.isBeforeFirst()){
                query = "SELECT * FROM hr WHERE id = ?";
                pst = con.prepareStatement(query);
                pst.setInt(1, id);
                rs = pst.executeQuery();
                if (!rs.next()){
                    return false;
                }
            }
            return rs.next();
        }catch(SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public void postJobOpening(){
        System.out.print("Enter Job Title: ");
        sc.nextLine();
        String title = sc.nextLine();
        System.out.print("Enter Job Description: ");
        String description = sc.nextLine();
        System.out.print("Enter Job Status (active/expired): ");
        String status = sc.nextLine();
        try{
            String query = "INSERT INTO job_openings (title, description, status) VALUES (?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, title);
            pst.setString(2, description);
            pst.setString(3, status);
            pst.executeUpdate();
            System.out.println("\n\tJob opening posted successfully!\n");
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public void viewJobOpenings(){
        try{
            String query = "SELECT * FROM job_openings";
            PreparedStatement pst = con.prepareStatement(query);
            ResultSet rs = pst.executeQuery();
            if(!rs.isBeforeFirst()) {
            	System.out.print("\n\tNo jobs have been posted by you\n");
            }
            while(rs.next()){
                System.out.println("\n\tJob ID: " + rs.getInt("id") + "\tTitle: "+rs.getString("title") + 
                		"\tDescription: "+rs.getString("description") + "\tStatus: "+rs.getString("status"));
                viewApplicantsForJob(rs.getInt("id"));
            }
            System.out.println();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public void viewApplicantsForJob(int jobId){
        try{
            String query = "SELECT applications.id, user.username, user.useremail, applications.status, applications.resume FROM applications " +
                           "JOIN user ON applications.user_id = user.id WHERE applications.job_id = ?";
//            PreparedStatement pst = con.prepareStatement(query);
            PreparedStatement pst = con.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
//            New learning: Commented out pst doesn't work in our case. why?
//            In Java, there are different types of ResultSet
//            	TYPE_FORWARD_ONLY (default) - It allows forward-only traversal (from the first row to the last row) 
//            									and does not support moving the cursor backward
//            TYPE_SCROLL_INSENSITIVE - Allows both forward and backward traversal. Changes in the database do not affect the result set.
//            TYPE_SCROLL_SENSITIVE: Similar to TYPE_SCROLL_INSENSITIVE, but changes in the database are reflected in the result set.
            
//            Since we are trying to move the cursor back to the initial position using rs.beforeFirst(), before the
//            inner while loop, the default resultSet cannot perform this operation.
            
//            Okay the why do we need something called ResultSet.CONCUR_READ_ONLY? That is known as "concurrency mode". 
//            It refers to the capability of a ResultSet to either be read-only or updatable. This mode determines whether the data 
//            in the ResultSet can be modified and, if so, whether those changes can be propagated back to the database.
//            If we attempt to create a scrollable result set without specifying the concurrency mode, it defaults to 
//            ResultSet.CONCUR_UPDATABLE, which might not be supported by our driver for scrollable result sets, resulting in errors.
            pst.setInt(1, jobId);
            ResultSet rs = pst.executeQuery();

            if(!rs.isBeforeFirst()){
                System.out.println("\n\tNo applicants for this job posting\n");
            }else{
                System.out.println("\n\tList of applicants who applied for this job:\n");
                while(rs.next()){
                    System.out.println("\tApplicant ID: " + rs.getInt("id") + "\tUsername: " + rs.getString("username") +
                                       "\tEmail: " + rs.getString("useremail") + "\tStatus: " + rs.getString("status"));
                }
                while (true) {
                    System.out.print("\nEnter the Applicant ID of whom you want to download the Resume (or 0 to exit): ");
                    int applicantId = sc.nextInt();
                    if (applicantId != 0) {
                        boolean applicantExists = false;
                        rs.beforeFirst(); 
                        while (rs.next()) {
                            if (rs.getInt("id") == applicantId) {
                                applicantExists = true;
                                break;
                            }
                        }
                        if (applicantExists) {
                            downloadResume(applicantId);
                        } else {
                            System.out.println("\n\tApplicant ID was not found in the applicants list.\n");
                        }
                    } else if (applicantId == 0){
                        return;
                    } else {
                    	System.out.print("\n\tEnter a valid input.\n");
                    }
                }     
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    private void downloadResume(int applicantId) {
        try {
            String query = "SELECT resume FROM applications WHERE id = ?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, applicantId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                String resumePath = rs.getString("resume");
                File resumeFile = new File(resumePath);
                if (!resumeFile.exists() || !resumeFile.isFile()) {
                    System.out.println("\n\tResume file not found.");
                    return;
                }
                System.out.print("Enter the destination file path to save the resume: ");
                sc.nextLine();
                String destPath = sc.nextLine();
                File destFile = new File(destPath);
                try {
                	FileInputStream fis = new FileInputStream(resumeFile);
                    FileOutputStream fos = new FileOutputStream(destFile);
                    byte[] res = fis.readAllBytes();
                    fos.write(res);
                    System.out.println("\n\tResume downloaded successfully.");
                    fis.close();
                    fos.close();
                } catch (IOException e) {
                    System.out.println("\n\tError occurred while downloading the resume.");
                    e.printStackTrace();
                }
            } else {
                System.out.println("\n\tApplication ID not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateJobOpening() {
        try {
            String checkQuery = "SELECT COUNT(*) FROM job_openings";
            PreparedStatement checkPst = con.prepareStatement(checkQuery);
            ResultSet rs = checkPst.executeQuery();
            rs.next();
            int jobCount = rs.getInt(1);

            if (jobCount == 0) {
                System.out.println("\n\tNo jobs has been posted by you\n");
            } else {
            	System.out.print("Enter Job ID: ");
                int jobId = sc.nextInt();
                sc.nextLine();
                System.out.print("Enter new Job Title: ");
                String title = sc.nextLine();
                System.out.print("Enter new Job Description: ");
                String description = sc.nextLine();
                System.out.print("Enter new Job Status (active/expired): ");
                String status = sc.nextLine();

                String query = "UPDATE job_openings SET title = ?, description = ?, status = ? WHERE id = ?";
                PreparedStatement pst = con.prepareStatement(query);
                pst.setString(1, title);
                pst.setString(2, description);
                pst.setString(3, status);
                pst.setInt(4, jobId);
                pst.executeUpdate();
                System.out.println("\n\tJob opening updated successfully!\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void deleteJobOpening(){
    	try {
    		String checkQuery = "SELECT COUNT(*) FROM job_openings";
            PreparedStatement checkPst = con.prepareStatement(checkQuery);
            ResultSet rs = checkPst.executeQuery();
            rs.next();
            int jobCount = rs.getInt(1);

            if (jobCount == 0) {
                System.out.println("\n\tNo jobs has been posted by you\n");
            } else {
            	 System.out.print("Enter Job ID: ");
                 int jobId = sc.nextInt();
                 String query = "DELETE FROM job_openings WHERE id = ?";
                 PreparedStatement pst = con.prepareStatement(query);
                 pst.setInt(1, jobId);
                 pst.executeUpdate();
                 System.out.println("\n\tJob opening deleted successfully!\n");
            }
    	}
        catch(SQLException e){
            e.printStackTrace();
        }
       
    }

    public void updateApplicantStatus(){
        System.out.print("Enter Job ID to update applicant status: ");
        int jobId = sc.nextInt();
        try{
        	String query = "SELECT * FROM  job_openings WHERE id = ?"; 
        	PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, jobId);
            ResultSet rs = pst.executeQuery();
            if(!rs.isBeforeFirst()){
                System.out.println("\n\tGiven Job Id doesn't exist.\n");
            }else{
            	query = "SELECT applications.id, user.username, applications.status FROM applications "
                		+ "JOIN user ON applications.user_id = user.id WHERE applications.job_id = ?";
                pst = con.prepareStatement(query);
                pst.setInt(1, jobId);
                rs = pst.executeQuery();
                if(!rs.isBeforeFirst()){
                    System.out.println("\n\tNo applicants for this job posting\n");
                }else{
                    while(rs.next()){
                        System.out.println("\n\tApplicant ID: " + rs.getInt("id") + ", Username: " + 
                    rs.getString("username") + ", Status: " + rs.getString("status"));
                    }
                    System.out.print("\tEnter Applicant ID to update status: ");
                    int applicantId = sc.nextInt();
                    System.out.print("\tEnter new status (pending/accepted/rejected): ");
                    String status = sc.next();
                    String updateQuery = "UPDATE applications SET status = ? WHERE id = ?";
                    PreparedStatement updatePstmt = con.prepareStatement(updateQuery);
                    updatePstmt.setString(1, status);
                    updatePstmt.setInt(2, applicantId);
                    updatePstmt.executeUpdate();
                    System.out.println("\n\tApplicant status updated successfully.\n");
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public void updateJobPostingStatus(){
        System.out.print("Enter Job ID to update job posting status: ");
        int jobId = sc.nextInt();
        try{
        	String query = "SELECT * FROM  job_openings WHERE id = ?"; 
        	PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, jobId);
            ResultSet rs = pst.executeQuery();
            if(!rs.isBeforeFirst()){
                System.out.println("\n\tGiven Job Id doesn't exist.\n");
            }else{
            	System.out.print("Enter new status (active/expired): ");
                String status = sc.next();
                query = "UPDATE job_openings SET status = ? WHERE id = ?";
                pst = con.prepareStatement(query);
                pst.setString(1, status);
                pst.setInt(2, jobId);
                pst.executeUpdate();
                System.out.println("\n\tJob posting status updated successfully.\n");
            }
          }
            catch(SQLException e){
            e.printStackTrace();
        }
    }
    
    public void HRdata(int hrId) {
        while (true) {
            System.out.println("1 - View HR Data\n2 - Delete HR Data\n0 - Back to Main Menu\n");
            System.out.print("Enter your choice: ");
            int choice = sc.nextInt();
            System.out.println();
            switch (choice) {
                case 1:
                    viewHRData(hrId);
                    break;
                case 2:
                    deleteHRData(hrId);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("\n\tInvalid choice. Please try again.\n");
            }
        }
    }

    public void viewHRData(int hrId) {
        try {
            String query = "SELECT * FROM hr WHERE id = ?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, hrId);
            ResultSet rs = pst.executeQuery();
            if (rs.next() && rs.getInt("id") == hrId) {
                if (rs.getString("name") == null || rs.getString("email") == null || rs.getString("date_of_joining") == null) {
                    System.out.println("\n\tYou haven't updated all of your details. Kindly enter them.\n");
                    updateHRData(hrId);
                } else {
                    System.out.println("\n\tName: " + rs.getString("name") +
                                       "\n\tEmail: " + rs.getString("email") +
                                       "\n\tDate of Joining: " + rs.getString("date_of_joining"));
                }
            } else {
                System.out.println("\n\tHR data not found.");
            }
            System.out.println();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateHRData(int hrId) {
        try {
            System.out.print("Enter your name: ");
            String name = sc.next();
            System.out.print("Enter your email: ");
            String email = sc.next();
            System.out.print("Enter your date of joining (YYYY-MM-DD): ");
            String dateOfJoining = sc.next();
            String Query = "UPDATE hr SET name = ?, email = ?, date_of_joining = ? WHERE id = ?";
            PreparedStatement pst = con.prepareStatement(Query);
            pst.setString(1, name);
            pst.setString(2, email);
            pst.setString(3, dateOfJoining);
            pst.setInt(4, hrId);
            int rowsUpdated = pst.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("\n\tHR details updated successfully.\n");
            } else {
                System.out.println("\n\tFailed to update HR details.\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void deleteHRData(int hrId) {
        try {
            String query = "DELETE FROM hr WHERE id = ?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, hrId);
            pst.executeUpdate();
            System.out.println("\n\tHR data deleted successfully.\n");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
