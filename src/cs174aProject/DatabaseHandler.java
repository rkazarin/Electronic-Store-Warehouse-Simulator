package cs174aProject;

import java.sql.*;


public class DatabaseHandler{
	
	String conn_descriptor = null;
	String conn_username = null;
	String conn_password = null;
	Connection db_conn = null;
	boolean conn_emart = false;
	boolean conn_edepot = false;
	boolean db_conn_init = false;
	
	public DatabaseHandler(String str_descriptor, String str_user, String str_pass, boolean eMart, boolean eDepot)
	{
		conn_descriptor = str_descriptor;
		conn_username = str_user;
		conn_password = str_pass;
		conn_emart = eMart;
		conn_edepot = eDepot;
		initializeConnection();
		try {
			checkTables();
		} catch (CustomException e) {
			e.printStackTrace();
		}
	}
	
	public ResultSet executeQuery(String query)
	{
		ResultSet r = null;
		Statement stmnt;
		try
		{
			stmnt = db_conn.createStatement();
			r = stmnt.executeQuery(query);
		}
		catch(SQLException e) {
			System.out.println("Connection Failed!");
			e.printStackTrace();
		}
		return r;
	}
	
	public int executeUpdate(String update)
	{
		Statement stmnt;
		try
		{
			stmnt = db_conn.createStatement();
			return stmnt.executeUpdate(update);
		}
		catch(SQLException e) {
			System.out.println("Connection Failed!");
			e.printStackTrace();
		}
		return 0;
	}
	
	public boolean initializeConnection()
	{
		
		//System.out.println("Oracle JDBC Connection Testing");
		
		try{
			DriverManager.registerDriver( new oracle.jdbc.OracleDriver());
			
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
		//System.out.println("Oracle JDBC Driver Registered");
		
		try
		{
			db_conn = DriverManager.getConnection(conn_descriptor,conn_username,conn_password);
			db_conn_init = true;
		}
		catch (SQLException e) {
			System.out.println("Connection Failed!");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	
	
	public void checkTables() throws CustomException
	{
		if(db_conn_init)
		{
			try {
				Statement stmnt = db_conn.createStatement();
				ResultSet rs = stmnt.executeQuery("SELECT table_name from user_tables");
				int emartCounter = 0;
				int edepotCounter = 0;
				while(rs.next())
				{
					if(conn_emart)
					{
						if(rs.getString(1).substring(0, 5).toLowerCase().equals("emart"))
						{
							emartCounter++;
						}
					}
					
					if(conn_edepot)
					{
						if(rs.getString(1).substring(0, 6).toLowerCase().equals("edepot"))
						{
							edepotCounter++;
						}		
					}
					
				}
				
				if(conn_emart == true)
				{
					if(emartCounter != 7)
					{
						throw new CustomException("Incorrect table count");
					}
				}
				
				if(conn_edepot == true)
				{
					if(edepotCounter != 1)
					{
						throw new CustomException("Incorrect table count");
					}
				}
				
				rs.close();
						
			}
			catch (SQLException e){
				e.printStackTrace();
			}
		}
		else
		{
			throw new CustomException("No DB connection");
		}

	}
	
}
