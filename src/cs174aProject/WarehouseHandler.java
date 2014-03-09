package cs174aProject;

import java.sql.*;
import java.util.Scanner;

//TODO: Receive a shipping notice from a manufacturer
//TODO: Receive a shipment
//TODO: Fill an order which has a unique order number, and a list of items sold(and their quantities).
//		If there are three or more items from the same manufacturer in the inventory that go below their respective stock level
//		send a replenishment order to the manufacturer

public class WarehouseHandler extends UserHandler {

	boolean logged_in = true;
	
	public WarehouseHandler(String dbDescription, String dbUser, String dbPassword)
	{
		super(true, true, dbDescription, dbUser, dbPassword);
	}
	
	public boolean processInput() {

		System.out.println("Type 'check quantity' to check quantity of item. Type 'receive notice' to receive a shipping notice."
				+ "Type 'receive shipment' to receive a shipment. Type 'fill order' to fill an order. Type 'logout' to exit system.");
		
		Scanner scan = new Scanner(System.in);
		String s = scan.nextLine();
		
		if(s.equals("check quantity"))
		{
			checkItemQuantity();
		}
		else if(s.equals("fill order"))
		{
			fillOrder();
		}
		else if(s.equals("logout"))
		{
			return false;
		}
		
		return true;
	}

	public void checkItemQuantity()
	{
		int quantity = 0;
		
		System.out.println("Enter the stock number of the product you want the quantity of: ");
		Scanner scan = new Scanner(System.in);
		String stockNum = scan.nextLine();
		
		try
		{
			Statement stmt = myDB.db_conn.createStatement();
			StringBuilder myQuery = new StringBuilder(500);
			myQuery.append("SELECT I.quantity");
			myQuery.append(" FROM EDEPOT_INVENTORY I");
			myQuery.append(" WHERE I.stock_num" + " = " + "\'" + stockNum + "\'");
			
			System.out.println(myQuery.toString());
			
			ResultSet rs = stmt.executeQuery(myQuery.toString());
			
			while(rs.next())
			{
				quantity = rs.getInt("QUANTITY");
			}
			
			rs.close();
		}
			
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		System.out.println("Stock_num " + stockNum + " has quantity: " + quantity);
			
	}
	
	public void fillOrder() {
		
	}
	
	
}
