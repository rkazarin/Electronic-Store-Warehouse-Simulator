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
			StringBuilder myQuery = new StringBuilder(150);
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
		
		//General idea: Query Emart_Orders for all unprocessed orders. For each of these orders, print out the items/price/quantities that belong to it
		try
		{
			Statement stmt = myDB.db_conn.createStatement();
			StringBuilder myQuery = new StringBuilder(150);
			myQuery.append("SELECT O.order_id");
			myQuery.append(" FROM EMART_ORDERS O");
			myQuery.append(" WHERE O.processed = 'FALSE'");
			//System.out.println(myQuery.toString());
			ResultSet rs = stmt.executeQuery(myQuery.toString());
			myQuery.setLength(0);
			
			while(rs.next())
			{
				String orderId = rs.getString("ORDER_ID");
				
				myQuery.append("SELECT I.stock_num, I.price, I.quantity");
				myQuery.append(" FROM EMART_ORDER_HAS_ITEM I");
				myQuery.append(" WHERE I.order_id = " + "\'" + orderId + "\'");
				//System.out.println(myQuery.toString());
				ResultSet rs2 = stmt.executeQuery(myQuery.toString());
				myQuery.setLength(0);
				
				System.out.println("Order_ID: " + orderId);
				
				while(rs2.next())
				{
					System.out.println("\t STOCK_NUM: " + rs2.getString("STOCK_NUM") 
							+ " PRICE: " + rs2.getString("PRICE") + " QUANTITY: " +rs2.getString("QUANTITY"));
					
				}
				
				rs2.close();
				
			}
			rs.close();
			
			System.out.print("Type the order_id of the order you want to fill: ");
			Scanner scan = new Scanner(System.in);
			String orderId = scan.nextLine();
			
			myQuery.append("SELECT I.stock_num, I.quantity");
			myQuery.append(" FROM EMART_ORDER_HAS_ITEM I");
			myQuery.append(" WHERE I.order_id = " + "\'" + orderId + "\'");
			//System.out.println(myQuery.toString());
			rs = stmt.executeQuery(myQuery.toString());
			myQuery.setLength(0);

			while(rs.next())
			{
				//get current stock_num and quantity of this item
				String stockNum = rs.getString("STOCK_NUM");
				int quantityPurchased = rs.getInt("QUANTITY");
				
				//get quantity of this product in warehouse
				myQuery.append("SELECT I.quantity");
				myQuery.append(" FROM EDEPOT_INVENTORY I");
				myQuery.append(" WHERE I.stock_num = " + "\'" + stockNum + "\'");
				ResultSet rs2 = stmt.executeQuery(myQuery.toString());
				myQuery.setLength(0);
				
				int quantityWarehouse = 0;
				
				while(rs2.next())
				{
					quantityWarehouse = rs2.getInt("QUANTITY");
				}
				
				int newWarehouseQuantity = quantityWarehouse - quantityPurchased;
				
				
				myQuery.append("UPDATE EDEPOT_INVENTORY");
				myQuery.append(" SET quantity = " + newWarehouseQuantity);
				myQuery.append(" WHERE stock_num = " + "\'" + stockNum + "\'");
				stmt.executeUpdate(myQuery.toString());
				
				myQuery.setLength(0);
				
				rs2.close();
				
			}
			
			//Finally, change processed to "TRUE" for that order_id in orders
			myQuery.append("UPDATE EMART_ORDERS");
			myQuery.append(" SET processed = 'TRUE'");
			myQuery.append(" WHERE order_id = " + "\'" + orderId + "\'");
			stmt.executeUpdate(myQuery.toString());
			
			
			rs.close();
			System.out.println("Order_Id: " + orderId + " has been filled.");
			
			
		}
		
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
	}
	
	public boolean isReplenishmentNeeded()
	{
		try
		{
			Statement stmt = myDB.db_conn.createStatement();
			StringBuilder myQuery = new StringBuilder(150);
			myQuery.append("SELECT O.order_id");
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	
}
