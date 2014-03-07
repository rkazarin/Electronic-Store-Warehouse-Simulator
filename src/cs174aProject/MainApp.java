package cs174aProject;

import java.sql.*;
import java.util.Scanner;

public class MainApp {

	static Connection conn;
	
	public static void main(String[] args) throws SQLException {
		
		//Open connection
		String strConn = "jdbc:oracle:thin:@uml.cs.ucsb.edu:1521:xe";
		String strUsername = "rkazarin";
		String strPassword = "6041651";
		boolean eMart = true;
		boolean eDepot = true;
		
		
		System.out.println("Enter 'c' for customer or 'm' for Manager");
		Scanner scan = new Scanner(System.in);
		String s = scan.nextLine();
		if(s.equals("c"))
		{
			System.out.println("Now, login as  a customer");
		}
		else if(s.equals("m")){
			ManagerHandler myManager = new ManagerHandler();
			myManager.establishUserConnection(strConn, strUsername, strPassword);
			try {
				myManager.myDB.checkTables();
			} catch (CustomException e) {
				e.printStackTrace();
			}
			System.out.println("Now, login as a manager");
			System.out.print("Username: ");
			String username = scan.nextLine();
			System.out.println("Password: ");
			String password = scan.nextLine();
			if(myManager.checkManagerLogin(username, password) == true)
			{
				System.out.println("You are now logged in as a manager");
			}
			else
			{
				System.out.println("You do not have manager permissions");
			}
			
			
		}

	}


}
