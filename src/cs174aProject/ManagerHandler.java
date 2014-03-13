package cs174aProject;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

//TODO: Print monthly summary of sales: the amount (quantity, price) of sale per product, per category, and the
//      customer who did the most purchase.


//TODO: Customer status adjustment according to the sales in the month.
//TODO: Send an order to a manufacturer (shipment will go to eDEPOT directly).
//TODO: Delete all sales transactions that are no longer needed in computing customer status. All orders need to
//      be stored unless they are explicitly deleted.


public class ManagerHandler extends UserHandler {
	boolean logged_in;
	
	public ManagerHandler(String dbDescription, String dbUser, String dbPassword){
		super(true, false, dbDescription, dbUser, dbPassword);	
		logged_in = false;
	}
	
	public void adjustCustomerStatus()
	{
		
		StringBuilder myQuery0 = new StringBuilder();
		myQuery0.append("UPDATE EMART_CUSTOMERS C1\n");
		myQuery0.append("SET C1.STATUS =\'New\'\n");
		myQuery0.append("WHERE C1.CUSTOMER_ID IN (\n");
		myQuery0.append("SELECT CUS.CUSTOMER_ID AS CID\n");
		myQuery0.append("FROM EMART_CUSTOMERS CUS\n");
		myQuery0.append("WHERE CUS.CUSTOMER_ID NOT IN (SELECT O.CUSTOMER_ID AS CID FROM EMART_ORDERS O))");
		
		StringBuilder myQuery = new StringBuilder();
		myQuery.append("UPDATE EMART_CUSTOMERS C1\n");
		myQuery.append("SET C1.STATUS = \'Green\'\n");
		myQuery.append("WHERE C1.CUSTOMER_ID IN (\n");
		myQuery.append("SELECT x.CID\n");
		myQuery.append("FROM (SELECT C.CUSTOMER_ID AS CID, SUM(D.OT) AS OCOST\n");
		myQuery.append("FROM EMART_CUSTOMERS C,	(SELECT O2.ORDER_ID AS OID,O2.CUSTOMER_ID AS CID,O2.ORDER_TOTAL AS OT, row_number() over (partition by O2.CUSTOMER_ID order by O2.ORDER_ID desc) rn\n");
		myQuery.append("FROM EMART_ORDERS O2 \n");
		myQuery.append("ORDER BY O2.CUSTOMER_ID, O2.ORDER_ID desc) D\n");
		myQuery.append("WHERE C.CUSTOMER_ID = D.CID AND D.rn <= 3\n");
		myQuery.append("GROUP BY C.CUSTOMER_ID) x\n");
		myQuery.append("WHERE x.OCOST BETWEEN (SELECT E1.GREEN_MIN FROM EMART_CHECKOUT_INFO E1 WHERE E1.ID = 0) AND (SELECT E1.SILVER_MIN FROM EMART_CHECKOUT_INFO E1 WHERE E1.ID = 0))");
		StringBuilder myQuery1 = new StringBuilder();
		myQuery1.append("UPDATE EMART_CUSTOMERS C1\n");
		myQuery1.append("SET C1.STATUS = \'Silver\'\n");
		myQuery1.append("WHERE C1.CUSTOMER_ID IN (\n");
		myQuery1.append("SELECT x.CID\n");
		myQuery1.append("FROM (SELECT C.CUSTOMER_ID AS CID, SUM(D.OT) AS OCOST\n");
		myQuery1.append("FROM EMART_CUSTOMERS C,	(SELECT O2.ORDER_ID AS OID,O2.CUSTOMER_ID AS CID,O2.ORDER_TOTAL AS OT, row_number() over (partition by O2.CUSTOMER_ID order by O2.ORDER_ID desc) rn\n");
		myQuery1.append("FROM EMART_ORDERS O2 \n");
		myQuery1.append("ORDER BY O2.CUSTOMER_ID, O2.ORDER_ID desc) D\n");
		myQuery1.append("WHERE C.CUSTOMER_ID = D.CID AND D.rn <= 3\n");
		myQuery1.append("GROUP BY C.CUSTOMER_ID) x\n");
		myQuery1.append("WHERE x.OCOST BETWEEN (SELECT E1.SILVER_MIN FROM EMART_CHECKOUT_INFO E1 WHERE E1.ID = 0) AND (SELECT E1.GOLD_MIN FROM EMART_CHECKOUT_INFO E1 WHERE E1.ID = 0))");
		StringBuilder myQuery2 = new StringBuilder();
		myQuery2.append("UPDATE EMART_CUSTOMERS C1 \n");
		myQuery2.append("SET C1.STATUS = \'Gold\' \n");
		myQuery2.append("WHERE C1.CUSTOMER_ID IN (\n");
		myQuery2.append("SELECT x.CID\n");
		myQuery2.append("FROM (SELECT C.CUSTOMER_ID AS CID, SUM(D.OT) AS OCOST\n");
		myQuery2.append("FROM EMART_CUSTOMERS C,	(SELECT O2.ORDER_ID AS OID,O2.CUSTOMER_ID AS CID,O2.ORDER_TOTAL AS OT, row_number() over (partition by O2.CUSTOMER_ID order by O2.ORDER_ID desc) rn\n");
		myQuery2.append("FROM EMART_ORDERS O2 \n");
		myQuery2.append("ORDER BY O2.CUSTOMER_ID, O2.ORDER_ID desc) D\n");
		myQuery2.append("WHERE C.CUSTOMER_ID = D.CID AND D.rn <= 3\n");
		myQuery2.append("GROUP BY C.CUSTOMER_ID) x\n");
		myQuery2.append("WHERE x.OCOST >= (SELECT E1.GOLD_MIN FROM EMART_CHECKOUT_INFO E1 WHERE E1.ID = 0))");
		int new_customers = myDB.executeUpdate(myQuery0.toString());
		int upgrade_to_green = myDB.executeUpdate(myQuery.toString());
		int upgrade_to_silver = myDB.executeUpdate(myQuery1.toString());
		int upgrade_to_gold = myDB.executeUpdate(myQuery2.toString());
		System.out.println("Number of Users who are now 'New' Status: "+Integer.toString(new_customers));
		System.out.println("Number of Users who are now 'Green' Status: "+Integer.toString(upgrade_to_green));
		System.out.println("Number of Users who are now 'Silver' Status: "+Integer.toString(upgrade_to_silver));
		System.out.println("Number of Users who are now 'Gold' Status: "+Integer.toString(upgrade_to_gold));

	}

	public void showMonthlySummary()
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
		
		Calendar cal = Calendar.getInstance();
		String time1 = dateFormat.format(cal.getTime());
		cal.add(Calendar.MONTH, -1);
		String time2 = dateFormat.format(cal.getTime());

		long lastMonthTime = cal.getTimeInMillis()/1000;
		StringBuilder myQuery = new StringBuilder();
		//myQuery.append("SELECT F.STOCK_NUMBER AS STOCK_NUMBER, F.TOTAL_QUANTITY AS TOTAL_QUANTITY, F.TOTAL_QUANTITY*F.PRICE AS TOTAL_PRICE\n");
		//myQuery.append("FROM ( ");
		myQuery.append("SELECT DISTINCT A.STOCK_NUM STOCK_NUMBER, SUM(A.QUANTITY) AS TOTAL_QUANTITY, SUM(A.PRICE*A.QUANTITY) AS TOTAL_REVENUE ");
		myQuery.append("FROM EMART_ORDER_HAS_ITEM A, EMART_ORDERS B \n");
		myQuery.append("WHERE A.ORDER_ID = B.ORDER_ID ");
		myQuery.append("AND B.ORDER_TIME > ");
		myQuery.append(lastMonthTime);
		myQuery.append(" GROUP BY A.STOCK_NUM ");
		StringBuilder myQuery2 = new StringBuilder();
		myQuery2.append("SELECT DISTINCT C.CATEGORY AS CATEGORY, SUM(I.QUANTITY) AS TOTAL_QUANTITY, SUM(I.PRICE*I.QUANTITY) TOTAL_REVENUE ");
		myQuery2.append("FROM EMART_CATALOG C, EMART_ORDER_HAS_ITEM I, EMART_ORDERs D ");
		myQuery2.append("WHERE D.ORDER_ID = I.ORDER_ID AND C.STOCK_NUM = I.STOCK_NUM AND D.ORDER_TIME > ");
		myQuery2.append(lastMonthTime);
		myQuery2.append('\n');
		myQuery2.append("GROUP BY C.CATEGORY");
		StringBuilder myQuery3 = new StringBuilder(150);
		myQuery3.append("SELECT DISTINCT C.CUSTOMER_ID AS CUSTOMER_ID, C.NAME AS CUSTOMER_NAME, SUM(D.ORDER_TOTAL) AS AGGREGATE_ORDER_COST\n");
		myQuery3.append("FROM EMART_CUSTOMERS C, EMART_ORDERS D \n");
		myQuery3.append("WHERE D.CUSTOMER_ID = C.CUSTOMER_ID AND D.ORDER_TIME > ");
		myQuery3.append(lastMonthTime);
		myQuery3.append("\n");
		myQuery3.append("GROUP BY C.NAME, C.CUSTOMER_ID \n");
		myQuery3.append("HAVING SUM(D.ORDER_TOTAL) >= ALL (SELECT DISTINCT SUM(O2.ORDER_TOTAL) FROM EMART_ORDERS O2, EMART_CUSTOMERS C2 WHERE O2.CUSTOMER_ID = C2.CUSTOMER_ID AND O2.ORDER_TIME > ");
		myQuery3.append(lastMonthTime);
		myQuery3.append("\n GROUP BY C2.NAME, C2.CUSTOMER_ID)");
		try{
			Statement stmnt1 = myDB.db_conn.createStatement();
			Statement stmnt2 = myDB.db_conn.createStatement();
			Statement stmnt3 = myDB.db_conn.createStatement();
			//System.out.println(myQuery3.toString());
			ResultSet rs1 = stmnt1.executeQuery(myQuery.toString());
			ResultSet rs2 = stmnt2.executeQuery(myQuery2.toString());
			ResultSet rs3 = stmnt3.executeQuery(myQuery3.toString());
			//System.out.println(myQuery.toString());
			System.out.println("Summary of Sales by Stock Number from "+time2+" to "+time1);
			displayResultSet(rs1);
			System.out.println("Summary of Sales by Item_Category from "+time2+" to "+time1);
			displayResultSet(rs2);
			System.out.println("Biggest Customer from "+time2+" to "+time1);
			displayResultSet(rs3);
			rs1.close();
			rs2.close();
			rs3.close();
		}catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		
	}
	
	public void deleteOldOrders()
	{
		StringBuilder myQuery = new StringBuilder();
		StringBuilder myQuery1 = new StringBuilder();
		myQuery.append("DELETE FROM EMART_ORDERS O1\n");
		myQuery.append("WHERE O1.ORDER_ID IN (\n");
		myQuery.append("SELECT x.OID\n");
		myQuery.append("FROM (SELECT D.OID AS OID\n");
		myQuery.append("FROM EMART_CUSTOMERS C,	(SELECT O2.ORDER_ID AS OID,O2.CUSTOMER_ID AS CID,O2.ORDER_TOTAL AS OT, row_number() over (partition by O2.CUSTOMER_ID order by O2.ORDER_ID desc) rn\n");
		myQuery.append("FROM EMART_ORDERS O2 \n");
		myQuery.append("ORDER BY O2.CUSTOMER_ID, O2.ORDER_ID desc) D\n");
		myQuery.append("WHERE C.CUSTOMER_ID = D.CID AND D.rn > 3) x)");
		myQuery1.append("DELETE FROM EMART_ORDER_HAS_ITEM O1\n");
		myQuery1.append("WHERE O1.ORDER_ID IN (\n");
		myQuery1.append("SELECT x.OID\n");
		myQuery1.append("FROM (SELECT D.OID AS OID\n");
		myQuery1.append("FROM EMART_CUSTOMERS C,	(SELECT O2.ORDER_ID AS OID,O2.CUSTOMER_ID AS CID,O2.ORDER_TOTAL AS OT, row_number() over (partition by O2.CUSTOMER_ID order by O2.ORDER_ID desc) rn\n");
		myQuery1.append("FROM EMART_ORDERS O2 \n");
		myQuery1.append("ORDER BY O2.CUSTOMER_ID, O2.ORDER_ID desc) D\n");
		myQuery1.append("WHERE C.CUSTOMER_ID = D.CID AND D.rn > 3) x)");
		int numDeletedHasItem = myDB.executeUpdate(myQuery1.toString());
		int numDeletedOrders = myDB.executeUpdate(myQuery.toString());
		System.out.println("Number of Deleted EMART_ORDER Records : " + Integer.toString(numDeletedOrders));	
		System.out.println("Number of Deleted ORDER_HAS_ITEM Records : " + Integer.toString(numDeletedHasItem));	
	}
	public void changeItemPrice()
	{
		Scanner scan = new Scanner(System.in);
		System.out.print("Enter item stock # : ");
		String stock_num = scan.nextLine();
		System.out.print("Enter desired price : ");
		double new_price = scan.nextDouble();
		
		try
		{
			Statement stmt = myDB.db_conn.createStatement();
			StringBuilder myQuery = new StringBuilder(150);
			myQuery.append("UPDATE EMART_CATALOG C\n");
			myQuery.append("SET C.PRICE = " + new_price +"\n");
			myQuery.append("WHERE C.STOCK_NUM = " + "\'" + stock_num + "\'");
			stmt.executeUpdate(myQuery.toString());
			System.out.println("Changed the price of " + stock_num + " to " + new_price);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}

	}
	
	public void changeCustomerStatus()
	{
		Scanner scan = new Scanner(System.in);
		System.out.print("Enter Customer ID : ");
		String cust_id = scan.nextLine();
		boolean incorrect_status = true;
		boolean abort = false;
		String new_status = null;
		while(incorrect_status)
		{
			System.out.print("Enter Status ('New','Green','Silver', 'Gold'): ");
			new_status = scan.nextLine();
			switch(new_status){
				case "Gold" :	incorrect_status = false; break;
				case "New"	:   incorrect_status = false; break;
				case "Silver": 	incorrect_status = false; break;
				case "Green":	incorrect_status = false; break;
				case "" :		incorrect_status = false; abort = true; break ;
				default : System.out.println("Invalid Customer Status, try again.");
							break;
			}
		}
		if(!abort && new_status != null)
			try
			{
				Statement stmt = myDB.db_conn.createStatement();
				StringBuilder myQuery = new StringBuilder(150);
				myQuery.append("UPDATE EMART_CUSTOMERS C\n");
				myQuery.append(" SET C.STATUS = " + "\'"+new_status+"\'\n");
				myQuery.append(" WHERE C.CUSTOMER_ID = " + "\'" + cust_id + "\'");
				int newVal = stmt.executeUpdate(myQuery.toString());
				if(newVal == 1)
					System.out.println("Changed the status of " + cust_id + " to " + new_status);
				else
					System.out.println("No customer with that Customer ID");
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
		
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
				isManager = rs.getString("MANAGER");
			}
			rs.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		 
		if(password.equals(correctPassword) && isManager.equals("TRUE")){
			logged_in = true;
			return true;
		}
		return false;
		
	}
	
	public void sendOrder()
	{
		
		System.out.println("You are generating a new order. This will make a new shipping notice.");
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
					rs.close();
					
				}
				else
				{
					System.out.println("This stock num already exists");
					stockNum = rs.getString("STOCK_NUM");
					stockNumberStatus.add("EXISTING");
					rs.close();
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
					rs.close();
					
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
	
	public boolean processInput()
	{
		if(logged_in == false)
		{
			return checkManagerLogin();
		}
		else{
			System.out.println("Type 'month' to view the monthly reports.");
			System.out.println("Type 'update status' to update the status of the customers.");
			System.out.println("Type 'trim' to delete unnecessary ORDERs records.");
			System.out.println("Type 'send order' to generate a shipping notice from a manufacturer.");
			System.out.println("Type 'change price' to change the price of an item");
			System.out.println("Type 'update customer status' to update the status of a specific customer to a specific status");
			Scanner scan = new Scanner(System.in);
			String s = scan.nextLine();
			switch(s){
				case "month"			: 	showMonthlySummary();
											break;
				case "send order"		:	sendOrder();
											break;
				case "update status" 	: 	adjustCustomerStatus();
											break;
				case "trim"				:	deleteOldOrders();
											break;
				case "change price"		:	changeItemPrice();
											break;
				case "update customer status" 	: 	changeCustomerStatus();
													break;
				case "logout" 			: 	return false;
											
			}
			return true;
		}	
	}	
}
