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

		boolean loggedIn = false;
		
		do{
			UserHandler myUser;
			
			//Ask user whether they are customer or manager
			System.out.print("Enter 'c' for customer or 'm' for Manager: ");
			Scanner scan = new Scanner(System.in);
			String s = scan.nextLine();
			
			if(s.equals("c"))
				myUser = new CustomerHandler(strConn, strUsername, strPassword);
				
			
			else if(s.equals("m"))
				myUser = new ManagerHandler(strConn, strUsername, strPassword);
			
			else
				continue;
			
			do
			{
				loggedIn = myUser.processInput();
					
				
			}while(loggedIn);

		}while(true);
	}	
}
