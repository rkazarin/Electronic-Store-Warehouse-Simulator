package cs174aProject;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.swing.text.html.HTMLDocument.Iterator;


public class CustomerHandler extends UserHandler {
	HashMap<String, Integer> shoppingCart = new HashMap<String, Integer>();
	
	boolean logged_in = false;
	
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
			System.out.println("Type 'shop' to search. Type 'add' to add item into cart. "
					+ "Type 'view' to view cart contents. Type 'checkout' to purchase what is in cart. "
					+ "Type 'logout' to logout");
			Scanner scan = new Scanner(System.in);
			String s = scan.nextLine();
			if(s.equals("shop"))
			{
				searchEmart();
			}
			if(s.equals("add"))
			{
				addItemsToCart();
			}
			if(s.equals("view"))
			{
				viewItemsInShoppingCart();
			}
			if(s.equals("logout"))
			{
				return false;
			}
		}
		
		
		return true;
		
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
		
		try
		{
			Statement stmt = myDB.db_conn.createStatement();
			StringBuilder myQuery = new StringBuilder(150);
			myQuery.append("SELECT *");
			myQuery.append(" FROM EMART_CATALOG C");
			
			if(!searchQuery.isEmpty())
			{
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
			
			System.out.println(myQuery.toString());
	
			ResultSet rs = stmt.executeQuery(myQuery.toString());
			ResultSetMetaData rsmd = rs.getMetaData();
			int numColumns = rsmd.getColumnCount();
			while(rs.next())
			{
				for(int i = 1; i <= numColumns; i++)
				{
					if(i > 1) System.out.print(",   ");
					String columnValue = rs.getString(i);
					System.out.print(rsmd.getColumnName(i) + ": " + columnValue);
				}
				System.out.println("");
			}
			
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

//	public void checkoutOrder()
//	{
//		
//	}
	
	
	

}
