package cs174aProject;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.ArrayList;

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
				+ "Type 'receive shipment' to receive a shipment. Type 'fill order' to fill an order. Type 'logout' to exit system."
				+ "Type 'practice' to run sample method");
		
		Scanner scan = new Scanner(System.in);
		String s = scan.nextLine();
		
		if(s.equals("check quantity"))
		{
			checkItemQuantity();
		}
		else if(s.equals("fill order"))
		{
			fillOrder();
			if(isReplenishmentNeeded())
			{
				sendReplenishmentOrder();
			}
		}
		else if(s.equals("receive notice"))
		{
			receiveShippingNotice();
		}
		else if(s.equals("practice"))
		{
			makeNewUniqueStockNum();
		}
		else if(s.equals("receive shipment"))
		{
			receiveShipment();
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

			HashMap<String, Integer> stockNumbers = new HashMap<String, Integer>();
			
			while(rs.next())
			{
				String stockNum = rs.getString("STOCK_NUM");
				int quantityPurchased = rs.getInt("QUANTITY");
				stockNumbers.put(stockNum, quantityPurchased);
				
			}
			

			for(Map.Entry<String, Integer> entry : stockNumbers.entrySet())
			{
				//get current stock_num and quantity of this item
				String stockNum = entry.getKey();
				System.out.println(stockNum);
				int quantityPurchased = entry.getValue();
				
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
			myQuery.append("SELECT I.manufacturer");
			myQuery.append(" FROM EDEPOT_INVENTORY I");
			myQuery.append(" WHERE I.quantity < I.min_stock_level");
			myQuery.append(" GROUP BY(I.manufacturer)");
			myQuery.append(" HAVING COUNT(*) >= 3");
			
			ResultSet rs = stmt.executeQuery(myQuery.toString());
			
			if(!rs.next())
			{
				System.out.println("No replenishment order needed.");
				return false;
			}
			else
			{
				System.out.println("Need to send replenishment order.");
				return true;
			}
			
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			return false;
		}
		
	}
	
	public void sendReplenishmentOrder() {
		
		int replenishmentOrderId = getLastReplenishmentOrderId();
		
		try
		{
			Statement stmt = myDB.db_conn.createStatement();
			StringBuilder myQuery = new StringBuilder(150);
			myQuery.append("SELECT I.manufacturer");
			myQuery.append(" FROM EDEPOT_INVENTORY I");
			myQuery.append(" WHERE I.quantity < I.min_stock_level");
			myQuery.append(" GROUP BY(I.manufacturer)");
			myQuery.append(" HAVING COUNT(*) >= 3");
						
			ResultSet rs = stmt.executeQuery(myQuery.toString());
			myQuery.setLength(0);
			
			while(rs.next())
			{
				String currentManufacturer = rs.getString("MANUFACTURER");
				System.out.println(currentManufacturer);
				
				myQuery.append("SELECT I.model_num");
				myQuery.append(" FROM EDEPOT_INVENTORY I");
				myQuery.append(" WHERE I.manufacturer = " + "\'" + currentManufacturer + "\'");
				myQuery.append(" AND I.quantity < I.max_stock_level");
				
				ResultSet rs2 = stmt.executeQuery(myQuery.toString());
				myQuery.setLength(0);
				
				ArrayList<String> modelNums = new ArrayList<String>();
				while(rs2.next())
				{
					String currentModelNum = rs2.getString("MODEL_NUM");
					modelNums.add(currentModelNum);
				}
				
				for(int i = 0; i < modelNums.size(); i++)
				{
					String modelNum = modelNums.get(i);
					
					myQuery.append("INSERT INTO EDEPOT_REPLENISHMENT_ORDER");
					myQuery.append(" (replenishment_order_id, manufacturer, model_num)");
					myQuery.append(" VALUES");
					myQuery.append(" (" + replenishmentOrderId + "," + "\'" + currentManufacturer + "\'" + "," + "\'" + modelNum + "\'" + ")");
					
					System.out.println(myQuery.toString());
					
					stmt.executeQuery(myQuery.toString());
					myQuery.setLength(0);
				}
				
				
				
			}
			
			System.out.println("Replenishment Order ID: " + replenishmentOrderId + " sent!");
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	int getLastReplenishmentOrderId()
	{
		int lastReplenishmentOrderId = 0;
		
		try
		{
			Statement stmt = myDB.db_conn.createStatement();
			StringBuilder myQuery = new StringBuilder(150);
			myQuery.append("SELECT MAX(O.REPLENISHMENT_ORDER_ID) AS OID");
			myQuery.append(" FROM EDEPOT_REPLENISHMENT_ORDER O");
			
			//System.out.println(myQuery.toString());
			ResultSet rs = stmt.executeQuery(myQuery.toString());
			
			while(rs.next())
			{
				lastReplenishmentOrderId = rs.getInt("OID");
			}
			
			rs.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		//System.out.println(lastOrderId);
		return lastReplenishmentOrderId + 1;
		
	}
	
	public void receiveShippingNotice()
	{
		System.out.println("Enter shipping company name: ");
		Scanner scan = new Scanner(System.in);
		String shippingCompany = scan.nextLine();
		System.out.println("Enter manufacturer name: ");
		String manufacturer = scan.nextLine();
		
		System.out.println("Enter model numbers and quantities.");
		
		ArrayList<String> modelNums = new ArrayList<String>();
		ArrayList<Integer> quantities = new ArrayList<Integer>();
		ArrayList<String> stockNums = new ArrayList<String>();
		ArrayList<String> stockNumberStatus = new ArrayList<String>();
		
		do
		{
			Scanner scan2 = new Scanner(System.in);
			
			System.out.println("Enter model number or type 'done' to complete shipping notice: ");
			String modelNum = scan2.nextLine();
			if(modelNum.equals("done")){
				break;
			}
			else
			{
				modelNums.add(modelNum);
			}
			System.out.println("Enter quantity for this model number: ");
			int quantity = scan2.nextInt();
			
			quantities.add(quantity);
			
			String stockNum = "";
			
			//Now that we have the manufacturer/model_num, query Inventory to see if the stockNum for this already exists
			try
			{
				Statement stmt = myDB.db_conn.createStatement();
				StringBuilder myQuery = new StringBuilder(150);
				myQuery.append("SELECT I.stock_num");
				myQuery.append(" FROM EDEPOT_INVENTORY I");
				myQuery.append(" WHERE I.manufacturer = " + "\'" + manufacturer + "\'" + " AND I.model_num = " + "\'" + modelNum + "\'");
				
				ResultSet rs = stmt.executeQuery(myQuery.toString());
				myQuery.setLength(0);
				
				if(!rs.next())
				{
					System.out.println("New product. Need to make a new stock_num");
					stockNum = makeNewUniqueStockNum();
					stockNumberStatus.add("NEW");
					
				}
				else
				{
					System.out.println("This stock num already exists");
					stockNum = rs.getString("STOCK_NUM");
					stockNumberStatus.add("EXISTING");
				}
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
			
			stockNums.add(stockNum);
			
		}while(true);
		
		//Now insert into shipping_notice and shipping_notice_has_items
		try
		{
			String shippingNoticeId = makeNewUniqueStockNum();
			
			Statement stmt = myDB.db_conn.createStatement();
			StringBuilder myQuery = new StringBuilder(150);
			myQuery.append("INSERT INTO EDEPOT_SHIPPING_NOTICE");
			myQuery.append(" (shipping_notice_id, shipping_company_name, received)");
			myQuery.append(" VALUES");
			myQuery.append(" (" + "\'" + shippingNoticeId + "\'" + "," + "\'" + shippingCompany + "\'" + "," + "\'" + "FALSE" + "\'" + ")");			
			stmt.executeQuery(myQuery.toString());
			myQuery.setLength(0);
			
			for(int i = 0; i < modelNums.size(); i++)
			{
				myQuery.append("INSERT INTO EDEPOT_SHIPPING_NOTICE_ITEMS");
				myQuery.append(" (shipping_notice_id, stock_num, manufacturer, model_num, quantity)");
				myQuery.append(" VALUES");
				myQuery.append(" (" + "\'" + shippingNoticeId + "\'" + "," + "\'" + stockNums.get(i) + "\'" + "," + "\'" + manufacturer 
						+ "\'" + "," + "\'" + modelNums.get(i) + "\'" + "," + quantities.get(i) + ")");			
				stmt.executeQuery(myQuery.toString());
				myQuery.setLength(0);
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		//Now, if the stockNum already existed, just update the replenishment quantity with the quantity.
		//However if the stockNum was newly generated, insert a new row into Inventory and prompt EDEPOT manager for remaining info.
		for(int i = 0; i < modelNums.size(); i++)
		{
			if(stockNumberStatus.get(i).equals("EXISTING"))
			{
				try
				{
					Statement stmt = myDB.db_conn.createStatement();
					StringBuilder myQuery = new StringBuilder(150);
					myQuery.append("UPDATE EDEPOT_INVENTORY");
					myQuery.append(" SET replenishment = replenishment + " + quantities.get(i));
					myQuery.append(" WHERE stock_num = " + "\'" + stockNums.get(i) + "\'");
					
					ResultSet rs = stmt.executeQuery(myQuery.toString());
					myQuery.setLength(0);
					
				}
				catch(SQLException e)
				{
					e.printStackTrace();
				}
			}
			
			else
			{
				
				System.out.println("A new product is being inserted into the Inventory.");
				System.out.println("Please enter minimum stock level for: " + manufacturer + ": " + modelNums.get(i));
				Scanner scan3 = new Scanner(System.in);
				int minStockLevel = scan3.nextInt();
				System.out.println("Please enter maximum stock level for this product: ");
				int maxStockLevel = scan3.nextInt();
				System.out.println("Please enter the location of this product: ");
				Scanner scan4 = new Scanner(System.in);
				String location = scan4.nextLine();
				
				try
				{
					Statement stmt = myDB.db_conn.createStatement();
					StringBuilder myQuery = new StringBuilder(150);
					myQuery.append("INSERT INTO EDEPOT_INVENTORY");
					myQuery.append(" (stock_num, manufacturer, model_num, quantity, min_stock_level, max_stock_level, location, replenishment)");
					myQuery.append(" VALUES");
					myQuery.append(" (" + "\'" + stockNums.get(i) + "\'" + "," + "\'" + manufacturer + "\'" + "," + "\'" + modelNums.get(i) + "\'" 
									+ "," + 0 + "," + minStockLevel + "," + maxStockLevel + "," + "\'" + location + "\'" + "," + quantities.get(i) + ")");			
					System.out.println(myQuery.toString());
					stmt.executeQuery(myQuery.toString());
					myQuery.setLength(0);
										
				}
				catch(SQLException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		
	}
	
	public String makeNewUniqueStockNum()
	{
		//Ascii of 'A' = 65;
		//Ascii of 'Z' = 90;
		
		String newStockNum = "";
		
		while(true)
		{
		//For leading capital letter
			int asciiLetter1 = 65 + (int)(Math.random() * ((90-65) + 1));
			int asciiLetter2 = 65 + (int)(Math.random() * ((90-65) + 1));
			
			//For the 5 numbers (0-9)
			//Ascii for '0' = 48
			//Ascii for '9' = 57
			int asciiNum1 = 48 + (int)(Math.random() * ((57-48) + 1));
			int asciiNum2 = 48 + (int)(Math.random() * ((57-48) + 1));
			int asciiNum3 = 48 + (int)(Math.random() * ((57-48) + 1));
			int asciiNum4 = 48 + (int)(Math.random() * ((57-48) + 1));
			int asciiNum5 = 48 + (int)(Math.random() * ((57-48) + 1));
			
			String letter1 = Character.toString((char)asciiLetter1);
			String letter2 = Character.toString((char)asciiLetter2);
			String num1 = Character.toString((char)asciiNum1);
			String num2 = Character.toString((char)asciiNum2);
			String num3 = Character.toString((char)asciiNum3);
			String num4 = Character.toString((char)asciiNum4);
			String num5 = Character.toString((char)asciiNum5);
			
			newStockNum = letter1 + letter2 + num1 + num2 + num3 + num4 + num5;
			
			System.out.println(newStockNum);
						
			try
			{
				Statement stmt = myDB.db_conn.createStatement();
				StringBuilder myQuery = new StringBuilder(150);
				myQuery.append("SELECT I.stock_num");
				myQuery.append(" FROM EDEPOT_INVENTORY I");
				myQuery.append(" WHERE I.stock_num = " + "\'" + newStockNum + "\'");
				
				//System.out.println(myQuery.toString());
				ResultSet rs = stmt.executeQuery(myQuery.toString());
				
				if(!rs.next())
				{
					System.out.println("This stock num is new!");
					rs.close();
					break;
				}
				else
				{
					System.out.println("This stock num already exists");
					rs.close();
					continue;
				}
				
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
		
		}
		
		return newStockNum;

	}
	
	
	public void receiveShipment()
	{
		//First list all unreceived shipping notices
		try
		{
			Statement stmt = myDB.db_conn.createStatement();
			StringBuilder myQuery = new StringBuilder(150);
			myQuery.append("SELECT I.stock_num");
			myQuery.append(" FROM EDEPOT_INVENTORY I");
			myQuery.append(" WHERE I.manufacturer = " + "\'" + manufacturer + "\'" + " AND I.model_num = " + "\'" + modelNum + "\'");
			
			ResultSet rs = stmt.executeQuery(myQuery.toString());
			myQuery.setLength(0);
			

			
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	
	}
	
	
}
