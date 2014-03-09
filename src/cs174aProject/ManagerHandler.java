package cs174aProject;
import java.sql.*;
import java.util.Scanner;

//TODO: Print monthly summary of sales: the amount (quantity, price) of sale per product, per category, and the
//      customer who did the most purchase.

//TODO: Customer status adjustment according to the sales in the month.
//TODO: Send an order to a manufacturer (shipment will go to eDEPOT directly).
//TODO: Change the price of an item
//TODO: Delete all sales transactions that are no longer needed in computing customer status. All orders need to
//      be stored unless they are explicitly deleted.


public class ManagerHandler extends UserHandler {

	
	public ManagerHandler(String dbDescription, String dbUser, String dbPassword){
		super(true, false, dbDescription, dbUser, dbPassword);	}
	
	public int changeItemPrice(int stock_id, float new_price)
	{
		StringBuilder myQuery = new StringBuilder(150);
		myQuery.append("UPDATE emart_catalog SET prices = ");
		myQuery.append(new_price);
		myQuery.append(" WHERE stock_num = ");
		myQuery.append(stock_id);
		//myQuery.append(";");
		return myDB.executeUpdate(myQuery.toString());
		
	}
	
	public boolean checkManagerLogin()
	{
		Scanner scan = new Scanner(System.in);
		System.out.print("Enter manager username: ");
		String username = scan.nextLine();
		System.out.print("Enter manager password: ");
		String password = scan.nextLine();
		
		String correctPassword = "";
		String isManager = "";
		
		try{
			Statement stmt = myDB.db_conn.createStatement();
			StringBuilder myQuery = new StringBuilder(150);
			myQuery.append("SELECT C.password, C.manager");
			myQuery.append(" FROM EMART_CUSTOMERS C");
			myQuery.append(" WHERE C.customer_ID = " + "\'" + username + "\'");
			
			ResultSet rs = stmt.executeQuery(myQuery.toString());
			
			while(rs.next())
			{
				correctPassword  = rs.getString("PASSWORD");
				System.out.println(correctPassword);
				isManager = rs.getString("MANAGER");
			}
			
			rs.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		if(password.equals(correctPassword) && isManager.equals("TRUE"))
		{
			return true;
		}
		
		else
		{
			return false;
		}
	}
	
	
	public boolean processInput()
	{
		return true;
		
	}	
}
