package cs174aProject;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.swing.text.html.HTMLDocument.Iterator;

//Todo: Recalculate status after each order

public class CustomerHandler extends UserHandler {
	HashMap<String, Integer> shoppingCart = new HashMap<String, Integer>();
	
	boolean logged_in = false;
	String customerID = "";
	
	public CustomerHandler(String dbDescription, String dbUser, String dbPassword){
		super(true, false, dbDescription, dbUser, dbPassword);
	}
	
	public boolean checkCustomerLogin()
	{
		Scanner scan = new Scanner(System.in);
		System.out.print("Enter customer username: ");
		String username = scan.nextLine();
		System.out.print("Enter customer password: ");
		String password = scan.nextLine();
		
		String correctPassword = "";
		
		try{
			Statement stmt = myDB.db_conn.createStatement();
			StringBuilder myQuery = new StringBuilder(150);
			myQuery.append("SELECT C.password");
			myQuery.append(" FROM EMART_CUSTOMERS C");
			myQuery.append(" WHERE C.customer_ID = " + "\'" + username + "\'");
			
			ResultSet rs = stmt.executeQuery(myQuery.toString());
			
			while(rs.next())
			{
				correctPassword  = rs.getString("PASSWORD");
			}
			
			rs.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			return false;
		}
		
		if(password.equals(correctPassword))
		{
			System.out.println("You are now logged in as customer: " + username);
			logged_in = true;
			customerID = username;
			return true;
		}
		
		else
		{
			System.out.println("Incorrect login info");
			return false;
		}
	}
	
	public boolean processInput()
	{
		if(logged_in == false)
		{
			return checkCustomerLogin();
		}
		else{
			System.out.println("Type 'shop' to search. Type 'add' to add item into cart.\n "
					+ "Type 'view' to view cart contents. Type 'delete' to delete an item from shopping cart. \n Type 'checkout' to purchase what is in cart. "
					+ "Type 'list past orders' to see all previous orders" +"Type 'lookup past order' to see all previous orders"+ "Type 'rerun' to rerun a previous order" + "Type 'logout' to logout");
			
			Scanner scan = new Scanner(System.in);
			String s = scan.nextLine();
			if(s.equals("shop"))
			{
				searchEmart();
			}
			else if(s.equals("add"))
			{
				addItemsToCart();
			}
			else if(s.equals("view"))
			{
				viewItemsInShoppingCart();
			}
			else if(s.equals("delete"))
			{
				deleteItemInShoppingCart();
			}
			else if(s.equals("list past orders"))
			{
				viewPreviousOrders();
			}
			else if(s.equals("lookup past order"))
			{
				lookupPastOrder();
			}
			else if(s.equals("checkout"))
			{
				checkoutOrder();
			}
			else if(s.equals("rerun"))
			{
				rerunOrder();
			}
			else if(s.equals("logout"))
			{
				return false;
			}
		}
		
		
		return true;
		
	}
	public void lookupPastOrder()
	{
		Scanner scan = new Scanner(System.in);

		int order_id = -1;
		while (order_id == -1)
		{
			System.out.print("Enter order number : ");
			String str = scan.nextLine();
			try{
			  order_id = Integer.parseInt(str);
			}catch (NumberFormatException e){
				System.out.println("Incorrect Format");
			}
		}
		StringBuilder myQuery = new StringBuilder();
		StringBuilder myQuery1 = new StringBuilder();
		myQuery.append("SELECT O.ORDER_ID AS ORDER_ID, O.PROCESSED AS ORDER_PROCESSED\n");
		myQuery.append("FROM EMART_ORDERS O\n");
		myQuery.append("WHERE O.ORDER_ID = " + Integer.toString(order_id) +" AND O.CUSTOMER_ID = "+"\'"+customerID+"\'\n");
		myQuery1.append("SELECT O.STOCK_NUM AS STOCK_NUMBER, O.PRICE AS UNIT_PRICE, O.QUANTITY AS QUANTITY\n");
		myQuery1.append("FROM EMART_ORDER_HAS_ITEM O, EMART_ORDERS O2\n");
		myQuery1.append("WHERE O.ORDER_ID = "+ Integer.toString(order_id) +" AND O2.CUSTOMER_ID = "+"\'"+customerID+"\' AND O.ORDER_ID = O2.ORDER_ID \n");
		ResultSet myRS = myDB.executeQuery(myQuery.toString());
		System.out.println("Order Information :");
		displayResultSet(myRS);
		ResultSet myRS2 = myDB.executeQuery(myQuery1.toString());
		System.out.println("Order Contents :");
		displayResultSet(myRS2);


	}
	public void searchEmart()
	{
		String stockNum = "";
		String manufacturer = "";
		String modelNum = "";
		String category = "";
		
		HashMap<String, String> searchQuery = new HashMap<String, String>();
		
		Scanner scan = new Scanner(System.in);
		System.out.println("Enter the stock number of product. Otherwise press [enter].");
		String s = scan.nextLine();
		if(!s.equals(""))
		{
			stockNum = s;
			searchQuery.put("stock_num", stockNum);
		}
		
		System.out.println("Enter the manufacturer of product. Otherwise press [enter].");
		s = scan.nextLine();
		if(!s.equals(""))
		{
			manufacturer = s;
			searchQuery.put("manufacturer", manufacturer);
		}
		
		System.out.println("Enter the model number of product. Otherwise press [enter].");
		s = scan.nextLine();
		if(!s.equals(""))
		{
			modelNum = s;
			searchQuery.put("model_num", modelNum);
		}
		
		System.out.println("Enter category of product. Otherwise press [enter].");
		s = scan.nextLine();
		if(!s.equals(""))
		{
			category = s;
			searchQuery.put("category", category);
		}
		
		ArrayList<String> descriptionAttributes = new ArrayList<String>();
		ArrayList<String> descriptionValues = new ArrayList<String>();
		boolean searchByDescription = false;
		
		System.out.println("Enter 'd' if you want to search by description. If not press [enter]: ");
		s = scan.nextLine();
		if(s.equals("d"))
		{
			searchByDescription = true;
			
			do
			{
				System.out.println("Enter description attribute or press 'done' if no more: ");
				Scanner scan2 = new Scanner(System.in);
				String attribute = scan2.nextLine();
				if(attribute.equals("done"))
				{
					break;
				}
				System.out.println("Enter description value: ");
				String value = scan2.nextLine();
				descriptionAttributes.add(attribute);
				descriptionValues.add(value);
				
			}while(true);
		}
		
		boolean searchByAccessory = false;
		System.out.println("Enter the stock_num you want to find accessories of. Press [enter] otherwise: ");
		String productStockNum = "";
		s = scan.nextLine();
		StringBuilder accessoryQuery = new StringBuilder();
		
		if(!s.equals(""))
		{
			productStockNum = s;
			searchByAccessory = true;
			
			accessoryQuery.append("SELECT A.stock_num");
			accessoryQuery.append(" FROM EMART_ITEM_IS_ACCESSORY A");
			accessoryQuery.append(" WHERE A.accessory_of = " + "\'" + productStockNum + "\'");
			
			
		}
		
		try
		{
			Statement stmt = myDB.db_conn.createStatement();
			StringBuilder myQuery = new StringBuilder();
			
			if(searchQuery.isEmpty() && searchByDescription == false)
			{
				myQuery.append("SELECT C.stock_num");
				myQuery.append(" FROM EMART_CATALOG C");
			}
			
			else if(!searchQuery.isEmpty() && searchByDescription == false)
			{
				myQuery.append("SELECT C.stock_num");
				myQuery.append(" FROM EMART_CATALOG C");
				myQuery.append(" WHERE");
				int elementNumber = 0;
				for(Map.Entry<String, String> entry :  searchQuery.entrySet())
				{
					if(searchQuery.size() == 1)
					{
						myQuery.append(" C." + entry.getKey() + " = " + "\'" + entry.getValue() + "\'");
						break;
					}
					
					if(elementNumber == 0)
					{
						myQuery.append(" C." + entry.getKey() + " = " + "\'" + entry.getValue() + "\'");
					}
					else
					{
						myQuery.append(" AND C." + entry.getKey() + " = " + "\'" + entry.getValue() + "\'");
					}
					
					elementNumber++;
				}
			}
			
			else if(searchQuery.isEmpty() && searchByDescription == true)
			{
				myQuery.append("SELECT C.stock_num");
				myQuery.append(" FROM EMART_CATALOG C, EMART_ITEM_DESCRIPTION D");
				myQuery.append(" WHERE C.stock_num = D.stock_num");
				
				for(int i = 0; i < descriptionAttributes.size(); i++)
				{
					if(descriptionAttributes.size() == 1)
					{
						myQuery.append(" AND (D.attribute = " + "\'" + descriptionAttributes.get(i) + "\'" + "AND D.value = " + "\'" + descriptionValues.get(i) + "\'" + ")");
						break;
					}
					if(i == 0)
					{
						myQuery.append(" AND ((D.attribute = " + "\'" + descriptionAttributes.get(i) + "\'" + "AND D.value = " + "\'" + descriptionValues.get(i) + "\'" + ")");
					}
					else if(i == descriptionAttributes.size() - 1)
					{
						myQuery.append(" OR (D.attribute = " + "\'" + descriptionAttributes.get(i) + "\'" + "AND D.value = " + "\'" + descriptionValues.get(i) + "\'" + ")" + ")");
					}
					else
					{
						myQuery.append(" OR (D.attribute = " + "\'" + descriptionAttributes.get(i) + "\'" + "AND D.value = " + "\'" + descriptionValues.get(i) + "\'" + ")");
					}
					
				}
				
			}
			
			else if(!searchQuery.isEmpty() && searchByDescription == true)
			{
				myQuery.append("SELECT C.stock_num");
				myQuery.append(" FROM EMART_CATALOG C, EMART_ITEM_DESCRIPTION D");
				myQuery.append(" WHERE C.stock_num = D.stock_num");
				
				for(Map.Entry<String, String> entry :  searchQuery.entrySet())
				{
					myQuery.append(" AND C." + entry.getKey() + " = " + "\'" + entry.getValue() + "\'");

				}
				
				for(int i = 0; i < descriptionAttributes.size(); i++)
				{
					if(descriptionAttributes.size() == 1)
					{
						myQuery.append(" AND (D.attribute = " + "\'" + descriptionAttributes.get(i) + "\'" + "AND D.value = " + "\'" + descriptionValues.get(i) + "\'" + ")");
						break;
					}
					if(i == 0)
					{
						myQuery.append(" AND ((D.attribute = " + "\'" + descriptionAttributes.get(i) + "\'" + "AND D.value = " + "\'" + descriptionValues.get(i) + "\'" + ")");
					}
					else if(i == descriptionAttributes.size() - 1)
					{
						myQuery.append(" OR (D.attribute = " + "\'" + descriptionAttributes.get(i) + "\'" + "AND D.value = " + "\'" + descriptionValues.get(i) + "\'" + ")" + ")");
					}
					else
					{
						myQuery.append(" OR (D.attribute = " + "\'" + descriptionAttributes.get(i) + "\'" + "AND D.value = " + "\'" + descriptionValues.get(i) + "\'" + ")");
					}
					
				}
				
				
				
			}
			
			//System.out.println(myQuery.toString());
			//System.out.println(accessoryQuery.toString());
			StringBuilder finalSearchQuery = new StringBuilder();
			
			if(searchQuery.isEmpty() && searchByAccessory == true)
			{
				finalSearchQuery = accessoryQuery;
			}
			else if(searchQuery.isEmpty() && searchByAccessory == false)
			{
				finalSearchQuery = myQuery;
			}
			else if(!searchQuery.isEmpty() && searchByAccessory == true)
			{
				finalSearchQuery.append(myQuery);
				finalSearchQuery.append(" INTERSECT ");
				finalSearchQuery.append(accessoryQuery);
			}
			else if(!searchQuery.isEmpty() && searchByAccessory == false)
			{
				finalSearchQuery = myQuery;
			}
	
			//System.out.println(finalSearchQuery.toString());
			
			ResultSet rs = stmt.executeQuery(finalSearchQuery.toString());
			//ResultSetMetaData rsmd = rs.getMetaData();
			//int numColumns = rsmd.getColumnCount();
			
			ArrayList<String> stockNumberResults = new ArrayList<String>();
			while(rs.next())
			{
				stockNumberResults.add(rs.getString("STOCK_NUM"));
			}
			
			rs.close();
			
			for(int i = 0; i < stockNumberResults.size(); i++)
			{
				String stockNumber = stockNumberResults.get(i);
				try
				{
					Statement stmt2 = myDB.db_conn.createStatement();
					StringBuilder myQuery2 = new StringBuilder();
					myQuery2.append("SELECT *");
					myQuery2.append(" FROM EMART_CATALOG C");
					myQuery2.append(" WHERE C.stock_num " + " = " + "\'" + stockNumber + "\'");
					
					ResultSet rs2 = stmt.executeQuery(myQuery2.toString());
					ResultSetMetaData rsmd = rs2.getMetaData();
					int numColumns = rsmd.getColumnCount();
					
					while(rs2.next())
					{
						for(int j = 1; j <= numColumns; j++)
						{
							if(j > 1) System.out.print(",   ");
							String columnValue = rs2.getString(j);
							System.out.print(rsmd.getColumnName(j) + ": " + columnValue);
						}
						System.out.println("");
					}
					
					rs2.close();
					
				}
				catch(SQLException e)
				{
					e.printStackTrace();
				}				
			}
			
			rs.close();
			
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
	}
	
	
	public void addItemsToCart()
	{
		System.out.print("Enter the stock number of the product you wish to add to your cart: ");
		Scanner scan = new Scanner(System.in);
		String stockNumber = scan.nextLine();
		System.out.print("Enter the quantity of this product to add to your cart: ");
		int i = scan.nextInt();
		Integer quantity = new Integer(i);
		shoppingCart.put(stockNumber, quantity);
	}
	
	public void viewItemsInShoppingCart(){
		System.out.println("Items that are currently in your shopping cart: ");
		System.out.println("");
		for(Map.Entry<String, Integer> entry :  shoppingCart.entrySet()){
			System.out.println("Stock Number: " + entry.getKey() + " - " + "Quantity: " + entry.getValue());
		}
	}
	
	public void deleteItemInShoppingCart()
	{
		viewItemsInShoppingCart();
		System.out.println("Enter stock number of item to delete: ");
		Scanner scan = new Scanner(System.in);
		String stockNumber = scan.nextLine();
		
		shoppingCart.remove(stockNumber);
		viewItemsInShoppingCart();
	}

	public void checkoutOrder()
	{
		double subTotal = 0;
		
		for(Map.Entry<String, Integer> entry : shoppingCart.entrySet())
		{
			try
			{
				Statement stmt = myDB.db_conn.createStatement();
				StringBuilder myQuery = new StringBuilder(150);
				myQuery.append("SELECT C.price");
				myQuery.append(" FROM EMART_CATALOG C");
				myQuery.append(" WHERE C.stock_num " + " = " + "\'" + entry.getKey() + "\'");
				
				//System.out.println(myQuery.toString());
				ResultSet rs = stmt.executeQuery(myQuery.toString());
				
				while(rs.next())
				{
					double price = rs.getDouble("PRICE");
					//System.out.println("Price: " + price);
					int quantity = entry.getValue();
					//System.out.println("Quantity: " + quantity);
					subTotal += price * quantity;
				}
				
				rs.close();
			
			}catch(SQLException e)
			{
				e.printStackTrace();
			}
			
		}
		
		//System.out.println("Current subtotal: " + subTotal);
	
		//Compute status discount
		double discountPercentage = computeStatusDiscount();
		//Apply discountPercentage on subTotal
		double discount = subTotal * (discountPercentage/100);
		//Compute shipping waive amount, and percentage
		double shippingWaiveAmount = computeShippingWaiveAmount();
		double shippingPercentage = computeShippingPercentage();
				
		double shippingFee = 0;
		if(subTotal <= shippingWaiveAmount)
		{
			shippingFee = subTotal*(shippingPercentage/100);
		}
		
		int orderId = getLastOrderId()+1;
		double total = subTotal - discount + shippingFee;
		total = Math.round(total * 100.0) / 100.0;
		subTotal = Math.round(subTotal*100.0)/100.0;
		double subTotal_discount = Math.round((subTotal-discount)*100.0)/100.0;
		//shippingFee = Math.round(shippingFee * 100)/100.0;
		
		//System.out.println(total);
		insertOrder(orderId, total);
		printOrderConfirmation(orderId);
		
		System.out.println("Discount percentage: " + discountPercentage);
		System.out.println("Subtotal: " + subTotal);
		System.out.println("Discount amount: " + discount);
		System.out.println("Subtotal with discount: " + (subTotal_discount));
		System.out.println("Shipping fee: " + shippingFee);
		System.out.println("Total Order cost: " + total);
		
	}
	
	
	public void printOrderConfirmation(int order_id)
	{

		StringBuilder myQuery = new StringBuilder();
		myQuery.append("SELECT I.stock_num AS StockNumber, I.price AS UnitPrice, I.quantity AS Quantity\n");
		myQuery.append("FROM EMART_ORDER_HAS_ITEM I\n");
		myQuery.append(" WHERE I.order_id = " + Integer.toString(order_id));
		ResultSet myrs = myDB.executeQuery(myQuery.toString());
		displayResultSet(myrs);
		//myDB.closeLastStatement();
	}
	public double computeStatusDiscount()
	{
		
		double discountPercentage = 0;
		String status = "";
		
		try
		{
			Statement stmt = myDB.db_conn.createStatement();
			StringBuilder myQuery = new StringBuilder(150);
			myQuery.append("SELECT C.status");
			myQuery.append(" FROM EMART_CUSTOMERS C");
			myQuery.append(" WHERE C.Customer_ID " + " = " + "\'" + customerID + "\'");
			
			//System.out.println(myQuery.toString());
			ResultSet rs = stmt.executeQuery(myQuery.toString());
			
			while(rs.next())
			{
				status = rs.getString("STATUS");
			}
			
			myQuery.setLength(0);
			myQuery.append("SELECT C." + status + "_percent");
			myQuery.append(" FROM EMART_CHECKOUT_INFO C");
			
			//System.out.println(myQuery.toString());
			rs = stmt.executeQuery(myQuery.toString());
			
			while(rs.next())
			{
				discountPercentage = rs.getDouble(status + "_percent");
			}		
			
			rs.close();
			
		}
	
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		return discountPercentage;
		
	}
	
	double computeShippingWaiveAmount()
	{
		double waiveAmount = 0;
		
		try
		{
			Statement stmt = myDB.db_conn.createStatement();
			StringBuilder myQuery = new StringBuilder(150);
			myQuery.append("SELECT C.shipping_waive_amount");
			myQuery.append(" FROM EMART_CHECKOUT_INFO C");
			
			//System.out.println(myQuery.toString());
			ResultSet rs = stmt.executeQuery(myQuery.toString());
			
			while(rs.next())
			{
				waiveAmount = rs.getFloat("SHIPPING_WAIVE_AMOUNT");
			}
			
			rs.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		return waiveAmount;
	}
	
	double computeShippingPercentage()
	{
		double shippingPercentage = 0;
		
		try
		{
			Statement stmt = myDB.db_conn.createStatement();
			StringBuilder myQuery = new StringBuilder(150);
			myQuery.append("SELECT C.shipping_percent");
			myQuery.append(" FROM EMART_CHECKOUT_INFO C");
			
			//System.out.println(myQuery.toString());
			ResultSet rs = stmt.executeQuery(myQuery.toString());
			
			while(rs.next())
			{
				shippingPercentage = rs.getFloat("SHIPPING_percent");
			}
			
			rs.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		return shippingPercentage;
	}
	
	int getLastOrderId()
	{
		int lastOrderId = 0;
		
		try
		{
			Statement stmt = myDB.db_conn.createStatement();
			StringBuilder myQuery = new StringBuilder(150);
			myQuery.append("SELECT MAX(ORDER_ID) AS OID");
			myQuery.append(" FROM EMART_ORDERS O");
			
			//System.out.println(myQuery.toString());
			ResultSet rs = stmt.executeQuery(myQuery.toString());
			
			while(rs.next())
			{
				lastOrderId = rs.getInt("OID");
			}
			
			rs.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		//System.out.println(lastOrderId);
		return lastOrderId;
		
	}
	
	void insertOrder(int orderId, double orderTotal)
	{
		long unixTime = System.currentTimeMillis() / 1000L;
		
		//insert one row into EMART_ORDERS
		try
		{
			Statement stmt = myDB.db_conn.createStatement();
			StringBuilder myQuery = new StringBuilder(150);
			myQuery.append("INSERT INTO EMART_ORDERS");
			myQuery.append(" (order_id, customer_id, order_time, order_total, processed)");
			myQuery.append(" VALUES");
			myQuery.append(" (" + orderId + "," + "\'" + customerID + "\'" + "," + unixTime + "," + orderTotal + "," + "\'" + "FALSE" + "\'" + ")");
			
			System.out.println(myQuery.toString());
			ResultSet rs = stmt.executeQuery(myQuery.toString());
			rs.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		//insert all items in shopping cart into EMART_ORDER_HAS_ITEM
		for(Map.Entry<String, Integer> entry : shoppingCart.entrySet())
		{
			try
			{
				Statement stmt = myDB.db_conn.createStatement();
				StringBuilder myQuery = new StringBuilder(150);
				myQuery.append("SELECT C.price");
				myQuery.append(" FROM EMART_CATALOG C");
				myQuery.append(" WHERE C.stock_num " + " = " + "\'" + entry.getKey() + "\'");
				
				//System.out.println(myQuery.toString());
				ResultSet rs = stmt.executeQuery(myQuery.toString());
				
				while(rs.next())
				{
					double price = rs.getDouble("PRICE");
					myQuery.setLength(0);
					myQuery.append("INSERT INTO EMART_ORDER_HAS_ITEM");
					myQuery.append(" (order_id, stock_num, price, quantity)");
					myQuery.append(" VALUES");
					myQuery.append(" (" + orderId + "," + "\'" + entry.getKey() + "\'" + "," + price + "," + entry.getValue() + ")");
					
					//System.out.println(myQuery.toString());
					ResultSet rs2 = stmt.executeQuery(myQuery.toString());
					rs2.close();
				}
		
		
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
	
		}
		
		System.out.println("Your order has been processed. Your Order ID is: " + orderId);
		
		shoppingCart.clear();
		
	}
	
	public void viewPreviousOrders()
	{
		//General idea: Query Emart_Orders for all unprocessed orders. For each of these orders, print out the items/price/quantities that belong to it
		try
		{
			Statement stmt = myDB.db_conn.createStatement();
			StringBuilder myQuery = new StringBuilder(150);
			myQuery.append("SELECT O.order_id");
			myQuery.append(" FROM EMART_ORDERS O");
			myQuery.append(" WHERE O.customer_id = " + "\'" + customerID + "\'");
			//System.out.println(myQuery.toString());
			ResultSet rs = stmt.executeQuery(myQuery.toString());
			myQuery.setLength(0);
			
			ArrayList<String> orderIdResults = new ArrayList<String>();
			while(rs.next())
			{
				orderIdResults.add(rs.getString("ORDER_ID"));
			}
			
			rs.close();
			
			for(int i = 0; i < orderIdResults.size(); i++)
			{
				String orderId = orderIdResults.get(i);
				
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
			
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public void rerunOrder()
	{
		viewPreviousOrders();
		
		System.out.println("Enter the orderid of the order you would like to rerun: ");
		Scanner scan = new Scanner(System.in);
		String orderId = scan.nextLine();
		
		HashMap<String, Integer> rerunOrderCart = new HashMap<String, Integer>();
		
		try
		{
			Statement stmt = myDB.db_conn.createStatement();
			StringBuilder myQuery = new StringBuilder(150);
			
			myQuery.append("SELECT I.stock_num, I.quantity");
			myQuery.append(" FROM EMART_ORDER_HAS_ITEM I");
			myQuery.append(" WHERE I.order_id = " + "\'" + orderId + "\'");
			//System.out.println(myQuery.toString());
			ResultSet rs2 = stmt.executeQuery(myQuery.toString());
			myQuery.setLength(0);
			
			while(rs2.next())
			{
				rerunOrderCart.put(rs2.getString("STOCK_NUM"), rs2.getInt("QUANTITY"));
			}
			
			
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		
		double subTotal = 0;
		
		for(Map.Entry<String, Integer> entry : rerunOrderCart.entrySet())
		{
			try
			{
				Statement stmt = myDB.db_conn.createStatement();
				StringBuilder myQuery = new StringBuilder(150);
				myQuery.append("SELECT C.price");
				myQuery.append(" FROM EMART_CATALOG C");
				myQuery.append(" WHERE C.stock_num " + " = " + "\'" + entry.getKey() + "\'");
				
				//System.out.println(myQuery.toString());
				ResultSet rs = stmt.executeQuery(myQuery.toString());
				
				while(rs.next())
				{
					double price = rs.getDouble("PRICE");
					//System.out.println("Price: " + price);
					int quantity = entry.getValue();
					//System.out.println("Quantity: " + quantity);
					subTotal += price * quantity;
				}
				
				rs.close();
			
			}catch(SQLException e)
			{
				e.printStackTrace();
			}
			
		}
		
		//System.out.println("Current subtotal: " + subTotal);
	
		//Compute status discount
		double discountPercentage = computeStatusDiscount();
		//Apply discountPercentage on subTotal
		double discount = subTotal * (discountPercentage/100);
		//Compute shipping waive amount, and percentage
		double shippingWaiveAmount = computeShippingWaiveAmount();
		double shippingPercentage = computeShippingPercentage();
		
		//System.out.println(shippingPercentage);
		
		double shippingFee = 0;
		if(subTotal <= shippingWaiveAmount)
		{
			shippingFee = subTotal*(shippingPercentage/100);
		}
		
		int newOrderId = getLastOrderId()+1;
		double total = subTotal - discount + shippingFee;
		
		////////INSERTING ORDER/////////
		
		long unixTime = System.currentTimeMillis() / 1000L;
		
		//insert one row into EMART_ORDERS
		try
		{
			Statement stmt = myDB.db_conn.createStatement();
			StringBuilder myQuery = new StringBuilder(150);
			myQuery.append("INSERT INTO EMART_ORDERS");
			myQuery.append(" (order_id, customer_id, order_time, order_total, processed)");
			myQuery.append(" VALUES");
			myQuery.append(" (" + newOrderId + "," + "\'" + customerID + "\'" + "," + unixTime + "," + total + "," + "\'" + "FALSE" + "\'" + ")");
			
			System.out.println(myQuery.toString());
			ResultSet rs = stmt.executeQuery(myQuery.toString());
			rs.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		//insert all items in shopping cart into EMART_ORDER_HAS_ITEM
		for(Map.Entry<String, Integer> entry : rerunOrderCart.entrySet())
		{
			try
			{
				Statement stmt = myDB.db_conn.createStatement();
				StringBuilder myQuery = new StringBuilder(150);
				myQuery.append("SELECT C.price");
				myQuery.append(" FROM EMART_CATALOG C");
				myQuery.append(" WHERE C.stock_num " + " = " + "\'" + entry.getKey() + "\'");
				
				//System.out.println(myQuery.toString());
				ResultSet rs = stmt.executeQuery(myQuery.toString());
				
				while(rs.next())
				{
					double price = rs.getDouble("PRICE");
					myQuery.setLength(0);
					myQuery.append("INSERT INTO EMART_ORDER_HAS_ITEM");
					myQuery.append(" (order_id, stock_num, price, quantity)");
					myQuery.append(" VALUES");
					myQuery.append(" (" + newOrderId + "," + "\'" + entry.getKey() + "\'" + "," + price + "," + entry.getValue() + ")");
					
					//System.out.println(myQuery.toString());
					ResultSet rs2 = stmt.executeQuery(myQuery.toString());
					rs2.close();
				}
		
		
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
	
		}
		
		System.out.println("Your order has been processed. Your Order ID is: " + newOrderId);
		
	}
		
}
