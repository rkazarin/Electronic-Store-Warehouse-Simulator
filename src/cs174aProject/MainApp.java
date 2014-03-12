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
		boolean start_prog = true;
		boolean loggedIn = false;
		
		do{
			UserHandler myUser = null;
			
			//Ask user whether they are customer or manager
			System.out.print("Enter 'c' for customer, 'm' for Manager, 'w' to access warehouse or 'q' to quit: ");
			Scanner scan = new Scanner(System.in);
			String s = scan.nextLine();
			
			switch(s)
			{
				case "c":myUser = new CustomerHandler(strConn, strUsername, strPassword);
							break;
				case "m":myUser = new ManagerHandler(strConn, strUsername, strPassword);
							break;
				case "w":myUser = new WarehouseHandler(strConn, strUsername, strPassword);
							break;
				case "q":start_prog = false;
							break;
				default : System.out.println("Invalid input, try again.");
			}


			if(start_prog && myUser != null)
			{
				do
				{
					loggedIn = myUser.processInput();
				
				}while(loggedIn);
				myUser.myDB.db_conn.close();
			}

		}while(start_prog);
	}	
}
